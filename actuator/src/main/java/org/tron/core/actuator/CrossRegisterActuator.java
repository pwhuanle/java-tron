package org.tron.core.actuator;

import static org.tron.core.config.Parameter.ChainConstant.TRANSFER_FEE;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Commons;
import org.tron.common.utils.DecodeUtil;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.db.CrossRevokingStore;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.store.AccountStore;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.Protocol.Transaction.Result.code;
import org.tron.protos.contract.BalanceContract.RegisterCrossContract;

@Slf4j(topic = "actuator")
public class CrossRegisterActuator extends AbstractActuator {

  public CrossRegisterActuator() {
    super(ContractType.RegisterCrossContract, RegisterCrossContract.class);
  }

  /**
   * 向数据库插入注册记录
   * @param object
   * @return
   * @throws ContractExeException
   */
  @Override
  public boolean execute(Object object) throws ContractExeException {
    TransactionResultCapsule ret = (TransactionResultCapsule) object;
    if (Objects.isNull(ret)) {
      throw new RuntimeException(ActuatorConstant.TX_RESULT_NULL);
    }

    long fee = calcFee();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    CrossRevokingStore crossRevokingStore = chainBaseManager.getCrossRevokingStore();
    try {
      RegisterCrossContract registerCrossContract = any.unpack(RegisterCrossContract.class);
      byte[] ownerAddress = registerCrossContract.getCrossChainInfo().getOwnerAddress().toByteArray();
      String chainId = registerCrossContract.getCrossChainInfo().getChainId().toString();
      long burn = dynamicStore.getBurnedForRegisterCross();
      Commons.adjustBalance(accountStore, ownerAddress, -burn);
      Commons.adjustBalance(accountStore, accountStore.getBlackhole().createDbKey(), burn);
      crossRevokingStore.putCrossInfo(chainId, registerCrossContract.getCrossChainInfo().toByteArray());
      ret.setStatus(fee, code.SUCESS);
    } catch (BalanceInsufficientException | ArithmeticException | InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }
    return true;
  }

  /**
   * 1. 解析协议，判断数据库中是否存在chain_id，若存在则返回error，
   * 2. 若不存在，判断owner地址
   * 3. 判断owner财富
   * 4. 都符合后返回true
   * @return
   * @throws ContractValidateException
   */
  @Override
  public boolean validate() throws ContractValidateException {
    if (this.any == null) {
      throw new ContractValidateException(ActuatorConstant.CONTRACT_NOT_EXIST);
    }
    if (chainBaseManager == null) {
      throw new ContractValidateException(ActuatorConstant.STORE_NOT_EXIST);
    }
    CrossRevokingStore crossRevokingStore = chainBaseManager.getCrossRevokingStore();
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    if (!this.any.is(RegisterCrossContract.class)) {
      throw new ContractValidateException(
          "contract type error, expected type [RegisterCrossContract], real type [" + this.any
              .getClass() + "]");
    }
    final RegisterCrossContract registerCrossContract;
    try {
      registerCrossContract = any.unpack(RegisterCrossContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }

    byte[] chainId = registerCrossContract.getCrossChainInfo().getChainId().toByteArray();
    byte[] ownerAddress = registerCrossContract.getCrossChainInfo().getOwnerAddress().toByteArray();

    // 判断chain_id是否存在
    if (crossRevokingStore.getCrossInfoById(ByteArray.toStr(chainId)) != null) {
      throw new ContractValidateException("ChainId has already been registered!");
    }

    if (!DecodeUtil.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid ownerAddress!");
    }

    AccountCapsule ownerAccount = accountStore.get(ownerAddress);

    if (ownerAccount == null) {
      throw new ContractValidateException("Validate RegisterCrossActuator error, no OwnerAccount.");
    }

    long balance = ownerAccount.getBalance();

    if (balance <= dynamicStore.getBurnedForRegisterCross()) {
      throw new ContractValidateException("OwnerAccount balance must be greater than BURNED_FOR_REGISTER_CROSS.");
    }

    // todo:是否要检查剩余的4个参数

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return any.unpack(RegisterCrossContract.class).getCrossChainInfo().getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0L;
  }

}

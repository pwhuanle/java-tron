package org.tron.core.actuator;

import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.StringUtil;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract.UnfreezeAssetContract;
import org.tron.protos.Protocol.Account.Frozen;
import org.tron.protos.Protocol.Transaction.Result.code;

@Slf4j
public class UnfreezeAssetActuator extends AbstractActuator {

  UnfreezeAssetActuator(Any contract, Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute(TransactionResultCapsule ret) throws ContractExeException {
    long fee = calcFee();
    try {
      UnfreezeAssetContract unfreezeAssetContract = contract
          .unpack(UnfreezeAssetContract.class);

      ByteString ownerAddress = unfreezeAssetContract.getOwnerAddress();
      byte[] ownerAddressBytes = ownerAddress.toByteArray();

      AccountCapsule accountCapsule = dbManager.getAccountStore().get(ownerAddressBytes);
      long unfreezeAsset = 0L;
      List<Frozen> frozenList = Lists.newArrayList();
      frozenList.addAll(accountCapsule.getFrozenSupplyList());
      Iterator<Frozen> iterator = frozenList.iterator();
      long now = dbManager.getHeadBlockTimeStamp();
      while (iterator.hasNext()) {
        Frozen next = iterator.next();
        if (next.getExpireTime() <= now) {
          unfreezeAsset += next.getFrozenBalance();
          iterator.remove();
        }
      }

      accountCapsule.addAssetAmount(accountCapsule.getAssetIssuedName(), unfreezeAsset);
      accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
          .clearFrozenSupply().addAllFrozenSupply(frozenList).build());

      dbManager.getAccountStore().put(ownerAddressBytes, accountCapsule);

      ret.setStatus(fee, code.SUCESS);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }

    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    try {
      if (!contract.is(UnfreezeAssetContract.class)) {
        throw new ContractValidateException(
            "contract type error,expected type [UnfreezeAssetContract],real type[" + contract
                .getClass() + "]");
      }
      if (this.dbManager == null) {
        throw new ContractValidateException();
      }
      UnfreezeAssetContract unfreezeAssetContract = this.contract
          .unpack(UnfreezeAssetContract.class);
      ByteString ownerAddress = unfreezeAssetContract.getOwnerAddress();
      if (!Wallet.addressValid(ownerAddress.toByteArray())) {
        throw new ContractValidateException("Invalidate address");
      }

      if (!dbManager.getAccountStore().has(ownerAddress.toByteArray())) {
        String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
        throw new ContractValidateException(
            "Account[" + readableOwnerAddress + "] not exists");
      }

      AccountCapsule accountCapsule = dbManager.getAccountStore()
          .get(ownerAddress.toByteArray());

      if (accountCapsule.getFrozenSupplyCount() <= 0) {
        throw new ContractValidateException("no frozen supply balance");
      }

      if(accountCapsule.getAssetIssuedName().isEmpty()){
        throw new ContractValidateException("this account did not issue any asset");
      }

      long now = dbManager.getHeadBlockTimeStamp();
      long allowedUnfreezeCount = accountCapsule.getFrozenSupplyList().stream()
          .filter(frozen -> frozen.getExpireTime() <= now).count();
      if (allowedUnfreezeCount <= 0) {
        throw new ContractValidateException("It's not time to unfreeze asset supply");
      }

    } catch (Exception ex) {
      throw new ContractValidateException(ex.getMessage());
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(UnfreezeAssetContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }

}

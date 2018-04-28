package org.tron.core.actuator;

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
import org.tron.protos.Contract.UnfreezeBalanceContract;
import org.tron.protos.Protocol.Account.Frozen;
import org.tron.protos.Protocol.Transaction.Result.code;

@Slf4j
public class UnfreezeBalanceActuator extends AbstractActuator {

  UnfreezeBalanceActuator(Any contract, Manager dbManager) {
    super(contract, dbManager);
  }


  @Override
  public boolean execute(TransactionResultCapsule ret) throws ContractExeException {
    long fee = calcFee();
    try {
      UnfreezeBalanceContract unfreezeBalanceContract = contract
          .unpack(UnfreezeBalanceContract.class);

      AccountCapsule accountCapsule = dbManager.getAccountStore()
          .get(unfreezeBalanceContract.getOwnerAddress().toByteArray());
      long oldBalance = accountCapsule.getBalance();
      long unfreezeBalance = 0L;
      List<Frozen> frozenList = accountCapsule.getFrozenList();
      Iterator<Frozen> iterator = frozenList.iterator();
      long now = System.currentTimeMillis();
      while (iterator.hasNext()) {
        Frozen next = iterator.next();
        if (next.getExpireTime() <= now) {
          unfreezeBalance += next.getFrozenBalance();
          iterator.remove();
        }
      }

      accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
          .setBalance(oldBalance + unfreezeBalance)
          .clearFrozen().addAllFrozen(frozenList).build());
      dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);

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
      if (!contract.is(UnfreezeBalanceContract.class)) {
        throw new ContractValidateException(
            "contract type error,expected type [UnfreezeBalanceContract],real type[" + contract
                .getClass() + "]");
      }

      UnfreezeBalanceContract unfreezeBalanceContract = this.contract
          .unpack(UnfreezeBalanceContract.class);
      ByteString ownerAddress = unfreezeBalanceContract.getOwnerAddress();
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

      if (accountCapsule.getFrozenCount() <= 0) {
        throw new ContractValidateException("no frozenBalance");
      }

      long now = System.currentTimeMillis();
      long allowedUnfreezeCount = accountCapsule.getFrozenList().stream()
          .filter(frozen -> frozen.getExpireTime() <= now).count();
      if (allowedUnfreezeCount <= 0) {
        throw new ContractValidateException("no frozenBalance");
      }


    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ContractValidateException(ex.getMessage());
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(UnfreezeBalanceContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }

}

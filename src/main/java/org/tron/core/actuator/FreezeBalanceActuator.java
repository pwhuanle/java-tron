package org.tron.core.actuator;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.StringUtil;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract.FreezeBalanceContract;
import org.tron.protos.Protocol.Account.Frozen;
import org.tron.protos.Protocol.Transaction.Result.code;

@Slf4j
public class FreezeBalanceActuator extends AbstractActuator {

  FreezeBalanceActuator(Any contract, Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute(TransactionResultCapsule ret) throws ContractExeException {
    long fee = calcFee();
    try {
      FreezeBalanceContract freezeBalanceContract = contract.unpack(FreezeBalanceContract.class);

      AccountCapsule accountCapsule = dbManager.getAccountStore()
          .get(freezeBalanceContract.getOwnerAddress().toByteArray());

      long now = dbManager.getHeadBlockTimeStamp();
      long duration = freezeBalanceContract.getFrozenDuration() * 86_400_000;

      long newBandwidth = calculateBandwidth(accountCapsule.getBandwidth(), freezeBalanceContract);
      long newBalance = accountCapsule.getBalance() - freezeBalanceContract.getFrozenBalance();

      long currentFrozenBalance = accountCapsule.getFrozenBalance();
      long newFrozenBalance = freezeBalanceContract.getFrozenBalance() + currentFrozenBalance;

      Frozen newFrozen = Frozen.newBuilder()
          .setFrozenBalance(newFrozenBalance)
          .setExpireTime(now + duration)
          .build();

      long frozenCount = accountCapsule.getFrozenCount();
      assert (frozenCount >= 0);
      if (frozenCount == 0) {
        accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
            .addFrozen(newFrozen)
            .setBalance(newBalance)
            .setBandwidth(newBandwidth)
            .build());
      } else {
        assert frozenCount == 1;
        accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
            .setFrozen(0, newFrozen)
            .setBalance(newBalance)
            .setBandwidth(newBandwidth)
            .build()
        );
      }
      dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);

      ret.setStatus(fee, code.SUCESS);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }

    return true;
  }

  private long calculateBandwidth(long oldValue, FreezeBalanceContract freezeBalanceContract) {
    try {
      return Math.addExact(oldValue, Math.multiplyExact(freezeBalanceContract.getFrozenBalance(),
          Math.multiplyExact(freezeBalanceContract.getFrozenDuration(),
              dbManager.getDynamicPropertiesStore().getBandwidthPerCoinday())));
    }catch (ArithmeticException e){
      return Long.MAX_VALUE;
    }
  }

  @Override
  public boolean validate() throws ContractValidateException {
    try {
      if (!contract.is(FreezeBalanceContract.class)) {
        throw new ContractValidateException(
            "contract type error,expected type [FreezeBalanceContract],real type[" + contract
                .getClass() + "]");
      }
      if (this.dbManager == null) {
        throw new ContractValidateException();
      }
      FreezeBalanceContract freezeBalanceContract = this.contract
          .unpack(FreezeBalanceContract.class);
      ByteString ownerAddress = freezeBalanceContract.getOwnerAddress();
      if (!Wallet.addressValid(ownerAddress.toByteArray())) {
        throw new ContractValidateException("Invalidate address");
      }

      if (!dbManager.getAccountStore().has(ownerAddress.toByteArray())) {
        String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
        throw new ContractValidateException(
            "Account[" + readableOwnerAddress + "] not exists");
      }

      long frozenBalance = freezeBalanceContract.getFrozenBalance();
      if (frozenBalance <= 0) {
        throw new ContractValidateException("frozenBalance must be positive");
      }
      if (frozenBalance < 1_000_000L) {
        throw new ContractValidateException("frozenBalance must be more than 1TRX");
      }

      AccountCapsule accountCapsule = dbManager.getAccountStore()
          .get(ownerAddress.toByteArray());
      if (frozenBalance > accountCapsule.getBalance()) {
        throw new ContractValidateException("frozenBalance must be less than accountBalance");
      }

//      long maxFrozenNumber = dbManager.getDynamicPropertiesStore().getMaxFrozenNumber();
//      if (accountCapsule.getFrozenCount() >= maxFrozenNumber) {
//        throw new ContractValidateException("max frozen number is: " + maxFrozenNumber);
//      }

      long frozenDuration = freezeBalanceContract.getFrozenDuration();
      long minFrozenTime = dbManager.getDynamicPropertiesStore().getMinFrozenTime();
      long maxFrozenTime = dbManager.getDynamicPropertiesStore().getMaxFrozenTime();

      if (!(frozenDuration >= minFrozenTime && frozenDuration <= maxFrozenTime)) {
        throw new ContractValidateException(
            "frozenDuration must be less than " + maxFrozenTime + " days "
                + "and more than " + minFrozenTime + " days");
      }

    } catch (InvalidProtocolBufferException ex) {
      throw new ContractValidateException(ex.getMessage());
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(FreezeBalanceContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }

}

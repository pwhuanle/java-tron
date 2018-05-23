package org.tron.core.actuator;

import com.google.common.base.Preconditions;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.StringUtil;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.capsule.WitnessCapsule;
import org.tron.core.capsule.utils.TransactionUtil;
import org.tron.core.db.Manager;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract.WitnessCreateContract;
import org.tron.protos.Protocol.Transaction.Result.code;

@Slf4j
public class WitnessCreateActuator extends AbstractActuator {

  WitnessCreateActuator(final Any contract, final Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute(TransactionResultCapsule ret) throws ContractExeException {
    long fee = calcFee();
    try {
      final WitnessCreateContract witnessCreateContract = this.contract
          .unpack(WitnessCreateContract.class);
      this.createWitness(witnessCreateContract);
      ret.setStatus(fee, code.SUCESS);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    } catch (BalanceInsufficientException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }
    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    try {
      if (!this.contract.is(WitnessCreateContract.class)) {
        throw new ContractValidateException(
            "contract type error,expected type [AccountCreateContract],real type[" + this.contract
                .getClass() + "]");
      }

      final WitnessCreateContract contract = this.contract.unpack(WitnessCreateContract.class);
      String readableOwnerAddress = StringUtil.createReadableString(contract.getOwnerAddress());

      if (!Wallet.addressValid(contract.getOwnerAddress().toByteArray())) {
        throw new ContractValidateException("Invalidate address");
      }

      if (!TransactionUtil.validUrl(contract.getUrl().toByteArray())) {
        throw new ContractValidateException("Invalidate url");
      }

      Preconditions.checkArgument(
          this.dbManager.getAccountStore().has(contract.getOwnerAddress().toByteArray()),
          "account[" + readableOwnerAddress + "] not exists");

      AccountCapsule accountCapsule = this.dbManager.getAccountStore()
          .get(contract.getOwnerAddress().toByteArray());

      Preconditions.checkArgument(
          !this.dbManager.getWitnessStore().has(contract.getOwnerAddress().toByteArray()),
          "Witness[" + readableOwnerAddress + "] has existed");

      Preconditions.checkArgument(
          accountCapsule.getBalance() >= dbManager.getDynamicPropertiesStore()
              .getAccountUpgradeCost(),
          "balance < AccountUpgradeCost");
    } catch (final Exception ex) {
      throw new ContractValidateException(ex.getMessage());
    }
    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(WitnessCreateContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }

  private void createWitness(final WitnessCreateContract witnessCreateContract) throws BalanceInsufficientException {
    //Create Witness by witnessCreateContract
    final WitnessCapsule witnessCapsule = new WitnessCapsule(
        witnessCreateContract.getOwnerAddress(),
        0,
        witnessCreateContract.getUrl().toStringUtf8());

    logger.debug("createWitness,address[{}]", witnessCapsule.createReadableString());
    this.dbManager.getWitnessStore().put(witnessCapsule.createDbKey(), witnessCapsule);
    AccountCapsule accountCapsule = this.dbManager.getAccountStore()
        .get(witnessCapsule.createDbKey());
    accountCapsule.setIsWitness(true);
    this.dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    dbManager.adjustBalance(witnessCreateContract.getOwnerAddress().toByteArray(),
            -dbManager.getDynamicPropertiesStore().getAccountUpgradeCost());

    dbManager.adjustBalance(this.dbManager.getAccountStore().getBlackhole().createDbKey(),
            +dbManager.getDynamicPropertiesStore().getAccountUpgradeCost());
  }
}

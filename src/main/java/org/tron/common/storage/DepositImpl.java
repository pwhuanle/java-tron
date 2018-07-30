package org.tron.common.storage;

import static org.tron.common.runtime.utils.MUtil.convertToTronAddress;

import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.tron.common.runtime.vm.DataWord;
import org.tron.common.utils.StringUtil;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.capsule.StorageCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.AccountContractIndexStore;
import org.tron.core.db.AccountStore;
import org.tron.core.db.AssetIssueStore;
import org.tron.core.db.BlockStore;
import org.tron.core.db.CodeStore;
import org.tron.core.db.ContractStore;
import org.tron.core.db.Manager;
import org.tron.core.db.StorageStore;
import org.tron.core.db.TransactionStore;
import org.tron.core.db.VotesStore;
import org.tron.core.db.WitnessStore;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.ContractExeException;
import org.tron.protos.Protocol;

/**
 * @author Guo Yonggang
 * @since 27.04.2018
 */
public class DepositImpl implements Deposit {

  private Manager dbManager;
  private Deposit parent = null;
  private Deposit prevDeposit = null;
  private Deposit nextDeposit = null;

  private long beforeRunStorageSize = 0;
  private HashMap<Key, Value> accounCache = new HashMap<>();
  private HashMap<Key, Value> transactionCache = new HashMap<>();
  private HashMap<Key, Value> blockCache = new HashMap<>();
  private HashMap<Key, Value> witnessCache = new HashMap<>();
  private HashMap<Key, Value> blockIndexCache = new HashMap<>();
  private HashMap<Key, Value> codeCache = new HashMap<>();
  private HashMap<Key, Value> contractCache = new HashMap<>();
  private HashMap<Key, Value> storageCache = new HashMap<>();
  private HashMap<Key, Value> votesCache = new HashMap<>();
  private HashMap<Key, Value> assetIssueCache = new HashMap<>();
  private HashMap<Key, Value> accountContractIndexCache = new HashMap<>();

  private DepositImpl(Manager dbManager, DepositImpl parent, DepositImpl prev) {
    init(dbManager, parent, prev);
  }

  protected void init(Manager dbManager, DepositImpl parent, DepositImpl prev) {
    this.dbManager = dbManager;
    this.parent = parent;
    prevDeposit = prev;
    nextDeposit = null;
  }

  @Override
  public Manager getDbManager() {
    return dbManager;
  }

  private BlockStore getBlockStore() {
    return dbManager.getBlockStore();
  }

  private TransactionStore getTransactionStore() {
    return dbManager.getTransactionStore();
  }

  private ContractStore getContractStore() {
    return dbManager.getContractStore();
  }

  private WitnessStore getWitnessStore() {
    return dbManager.getWitnessStore();
  }

  private VotesStore getVotesStore() {
    return dbManager.getVotesStore();
  }

  private AccountStore getAccountStore() {
    return dbManager.getAccountStore();
  }

  private CodeStore getCodeStore() {
    return dbManager.getCodeStore();
  }

  private StorageStore getStorageStore() {
    return dbManager.getStorageStore();
  }

  private AssetIssueStore getAssetIssueStore() {
    return dbManager.getAssetIssueStore();
  }

  private AccountContractIndexStore getAccountContractIndexStore() {
    return dbManager.getAccountContractIndexStore();
  }

  @Override
  public Deposit newDepositChild() {
    return new DepositImpl(dbManager, this, null);
  }

  @Override
  public Deposit newDepositNext() {
    return nextDeposit = new DepositImpl(dbManager, null, this);
  }

  @Override
  public synchronized AccountCapsule createAccount(byte[] address, Protocol.AccountType type) {
    Key key = new Key(address);
    AccountCapsule account = new AccountCapsule(ByteString.copyFrom(address), type);
    accounCache.put(key, new Value(account.getData(), Type.VALUE_TYPE_CREATE));
    return account;
  }

  @Override
  public synchronized AccountCapsule createAccount(byte[] address, ByteString accountName,
      Protocol.AccountType type) {
    Key key = new Key(address);
    AccountCapsule account = new AccountCapsule(ByteString.copyFrom(address), accountName,
        type);

    accounCache.put(key, new Value(account.getData(), Type.VALUE_TYPE_CREATE));
    return account;
  }

  @Override
  public synchronized AccountCapsule getAccount(byte[] address) {
    Key key = new Key(address);
    if (accounCache.containsKey(key)) {
      return accounCache.get(key).getAccount();
    }

    AccountCapsule accountCapsule;
    if (parent != null) {
      accountCapsule = parent.getAccount(address);
    } else if (prevDeposit != null) {
      accountCapsule = prevDeposit.getAccount(address);
    } else {
      accountCapsule = getAccountStore().get(address);
    }

    if (accountCapsule != null) {
      accounCache.put(key, Value.create(accountCapsule.getData()));
    }
    return accountCapsule;
  }


  public synchronized BytesCapsule getContractByNormalAccount(byte[] address) {

    Key key = new Key(address);
    if (accountContractIndexCache.containsKey(key)) {
      return accountContractIndexCache.get(key).getBytes();
    }

    BytesCapsule contract;
    if (parent != null) {
      contract = parent.getContractByNormalAccount(address);
    } else if (prevDeposit != null) {
      contract = prevDeposit.getContractByNormalAccount(address);
    } else {
      contract = getAccountContractIndexStore().get(address);
    }

    if (contract != null) {
      accountContractIndexCache.put(key, Value.create(contract.getData()));
    }
    return contract;
  }

  @Override
  public synchronized void createContract(byte[] address, ContractCapsule contractCapsule) {
    Key key = Key.create(address);
    Value value = Value.create(contractCapsule.getData(), Type.VALUE_TYPE_CREATE);
    contractCache.put(key, value);
  }

  @Override
  public synchronized void createContractByNormalAccountIndex(byte[] address,
      BytesCapsule contractAddress) {
    Key key = new Key(address);
    accountContractIndexCache
        .put(key, Value.create(contractAddress.getData(), Type.VALUE_TYPE_CREATE));
  }

  @Override
  public synchronized ContractCapsule getContract(byte[] address) {
    Key key = Key.create(address);
    if (contractCache.containsKey(key)) {
      return contractCache.get(key).getContract();
    }

    ContractCapsule contractCapsule;
    if (parent != null) {
      contractCapsule = parent.getContract(address);
    } else if (prevDeposit != null) {
      contractCapsule = prevDeposit.getContract(address);
    } else {
      contractCapsule = getContractStore().get(address);
    }

    if (contractCapsule != null) {
      contractCache.put(key, Value.create(contractCapsule.getData()));
    }
    return contractCapsule;
  }

  @Override
  public synchronized void saveCode(byte[] codeHash, byte[] code) {
    Key key = Key.create(codeHash);
    Value value = Value.create(code, Type.VALUE_TYPE_CREATE);
    codeCache.put(key, value);
  }

  @Override
  public synchronized byte[] getCode(byte[] codeHash) {
    Key key = Key.create(codeHash);
    if (codeCache.containsKey(key)) {
      codeCache.get(key).getCode().getData();
    }

    byte[] code;
    if (parent != null) {
      code = parent.getCode(codeHash);
    } else if (prevDeposit != null) {
      code = prevDeposit.getCode(codeHash);
    } else {
      if (null == getCodeStore().get(codeHash)) {
        code = null;
      } else {
        code = getCodeStore().get(codeHash).getData();
      }
    }

    if (code != null) {
      codeCache.put(key, Value.create(code));
    }
    return code;
  }

    /*
    @Override
    public byte[] getCodeHash(byte[] address) {
        AccountCapsule accountCapsule = getAccount(address);
        return accountCapsule != null ? accountCapsule.getCodeHash() : null;
    }
    */

  @Override
  public synchronized StorageCapsule getStorage(byte[] address) {
    Key key = Key.create(address);
    if (storageCache.containsKey(key)) {
      return storageCache.get(key).getStorage();
    }

    // first access the storageCapsule
    StorageCapsule storageCapsule;
    if (this.parent != null) {
      storageCapsule = parent.getStorage(address);
    } else if (prevDeposit != null) {
      storageCapsule = prevDeposit.getStorage(address);
    } else {
      storageCapsule = getStorageStore().get(address);
    }

    if (storageCapsule != null) {
      storageCache.put(key, Value.create(storageCapsule.getData(), Type.VALUE_TYPE_NORMAL));
      this.beforeRunStorageSize += storageCapsule.getInstance().getSerializedSize();
    }
    return storageCapsule;
  }

  @Override
  public synchronized void addStorageValue(byte[] address, DataWord key, DataWord value) {
    address = convertToTronAddress(address);
    if (getAccount(address) == null) {
      return;
    }
    Key addressKey = Key.create(address);
    if (storageCache.containsKey(addressKey)) {
      StorageCapsule storageCapsule = storageCache.get(addressKey).getStorage();

      if (storageCapsule != null) {
        storageCapsule.put(key, value);
        Value V = Value.create(storageCapsule.getData(),
            Type.VALUE_TYPE_DIRTY | storageCache.get(addressKey).getType().getType());
        storageCache.put(addressKey, V);
      }
    } else {
      StorageCapsule storageCapsule = getStorage(address);
      if (storageCapsule == null) {
        Protocol.StorageItem.Builder builder = Protocol.StorageItem.newBuilder();
        builder.setContractAddress(ByteString.copyFrom(address));
        Protocol.StorageItem storageItem = builder.build();
        storageCapsule = new StorageCapsule(storageItem);
        storageCapsule.put(key, value);
        Value V = Value.create(storageCapsule.getData(), Type.VALUE_TYPE_CREATE);
        storageCache.put(addressKey, V);
      } else {
        storageCapsule.put(key, value);
        Value V = Value.create(storageCapsule.getData(),
            Type.VALUE_TYPE_DIRTY | storageCache.get(addressKey).getType().getType());
        storageCache.put(addressKey, V);
      }
    }
  }

  @Override
  public synchronized DataWord getStorageValue(byte[] address, DataWord key) {
    address = convertToTronAddress(address);
    if (getAccount(address) == null) {
      return null;
    }
    Key addressKey = Key.create(address);
    if (storageCache.containsKey(addressKey)) {
      StorageCapsule storageCapsule = storageCache.get(addressKey).getStorage();
      return storageCapsule.get(key);
    }

    StorageCapsule storageCapsule = getStorage(address);
    if (storageCapsule != null) {
      Value V = Value.create(storageCapsule.getData(), Type.VALUE_TYPE_NORMAL);
      storageCache.put(addressKey, V);
      return storageCapsule.get(key);
    } else {
      Protocol.StorageItem.Builder builder = Protocol.StorageItem.newBuilder();
      builder.setContractAddress(ByteString.copyFrom(address));
      Protocol.StorageItem storageItem = builder.build();
      storageCapsule = new StorageCapsule(storageItem);
      Value V = Value.create(storageCapsule.getData(), Type.VALUE_TYPE_CREATE);
      storageCache.put(addressKey, V);
      return storageCapsule.get(key);
    }
  }

  @Override
  public synchronized long getBalance(byte[] address) {
    AccountCapsule accountCapsule = getAccount(address);
    return accountCapsule == null ? 0L : accountCapsule.getBalance();
  }

  @Override
  public synchronized long addBalance(byte[] address, long value)
      throws ContractExeException {
    AccountCapsule accountCapsule = getAccount(address);
    if (accountCapsule == null) {
      accountCapsule = createAccount(address, Protocol.AccountType.Normal);
    }

    long balance = accountCapsule.getBalance();
    if (value == 0) {
      return balance;
    }

    if (value < 0 && balance < -value) {
      throw new ContractExeException(
          StringUtil.createReadableString(accountCapsule.createDbKey())
              + " insufficient balance");
    }
    accountCapsule.setBalance(Math.addExact(balance, value));
    Key key = Key.create(address);
    Value V = Value.create(accountCapsule.getData(),
        Type.VALUE_TYPE_DIRTY | accounCache.get(key).getType().getType());
    accounCache.put(key, V);
    return accountCapsule.getBalance();
  }

  @Override
  public TransactionCapsule getTransaction(byte[] trxHash) {
    Key key = Key.create(trxHash);
    if (transactionCache.containsKey(key)) {
      return transactionCache.get(key).getTransaction();
    }

    TransactionCapsule transactionCapsule;
    if (parent != null) {
      transactionCapsule = parent.getTransaction(trxHash);
    } else if (prevDeposit != null) {
      transactionCapsule = prevDeposit.getTransaction(trxHash);
    } else {
      try {
        transactionCapsule = getTransactionStore().get(trxHash);
      } catch (BadItemException e) {
        transactionCapsule = null;
      }
    }

    if (transactionCapsule != null) {
      transactionCache.put(key, Value.create(transactionCapsule.getData()));
    }
    return transactionCapsule;
  }

  @Override
  public BlockCapsule getBlock(byte[] blockHash) {
    Key key = Key.create(blockHash);
    if (blockCache.containsKey(key)) {
      return blockCache.get(key).getBlock();
    }

    BlockCapsule ret;
    try {
      if (parent != null) {
        ret = parent.getBlock(blockHash);
      } else if (prevDeposit != null) {
        ret = prevDeposit.getBlock(blockHash);
      } else {
        ret = getBlockStore().get(blockHash);
      }
    } catch (Exception e) {
      ret = null;
    }

    if (ret != null) {
      blockCache.put(key, Value.create(ret.getData()));
    }
    return ret;
  }

  @Override
  public long computeAfterRunStorageSize() {
    AtomicLong afterRunStorageSize = new AtomicLong();
    storageCache.forEach(((key, value) -> {
      afterRunStorageSize.addAndGet(value.getStorage().getInstance().getSerializedSize());
    }));
    return afterRunStorageSize.get();
  }

  @Override
  public long getBeforeRunStorageSize() {
    return beforeRunStorageSize;
  }


  @Override
  public void putAccount(Key key, Value value) {
    accounCache.put(key, value);
  }

  @Override
  public void putTransaction(Key key, Value value) {
    transactionCache.put(key, value);
  }

  @Override
  public void putBlock(Key key, Value value) {
    blockCache.put(key, value);
  }

  @Override
  public void putWitness(Key key, Value value) {
    witnessCache.put(key, value);
  }

  @Override
  public void putCode(Key key, Value value) {
    codeCache.put(key, value);
  }

  @Override
  public void putContract(Key key, Value value) {
    contractCache.put(key, value);
  }

  @Override
  public void putContractByNormalAccountIndex(Key key, Value value) {
    accountContractIndexCache.put(key, value);
  }

  @Override
  public void putStorage(Key key, Value value) {
    storageCache.put(key, value);
  }

  @Override
  public void putVotes(Key key, Value value) {
    votesCache.put(key, value);
  }

  private void commitAccountCache(Deposit deposit) {
    accounCache.forEach((key, value) -> {
      if (value.getType().isCreate() || value.getType().isDirty()) {
        if (deposit != null) {
          deposit.putAccount(key, value);
        } else {
          getAccountStore().put(key.getData(), value.getAccount());
        }
      }
    });
  }

  private void commitTransactionCache(Deposit deposit) {
    transactionCache.forEach((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putTransaction(key, value);
        } else {
          getTransactionStore().put(key.getData(), value.getTransaction());
        }
      }
    });
  }

  private void commitBlockCache(Deposit deposit) {
    blockCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putBlock(key, value);
        } else {
          getBlockStore().put(key.getData(), value.getBlock());
        }
      }
    }));
  }

  private void commitWitnessCache(Deposit deposit) {
    witnessCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putWitness(key, value);
        } else {
          getWitnessStore().put(key.getData(), value.getWitness());
        }
      }
    }));
  }

  private void commitCodeCache(Deposit deposit) {
    codeCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putCode(key, value);
        } else {
          getCodeStore().put(key.getData(), value.getCode());
        }
      }
    }));
  }

  private void commitContractCache(Deposit deposit) {
    contractCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putContract(key, value);
        } else {
          getContractStore().put(key.getData(), value.getContract());
        }
      }
    }));
  }

  private void commitStorageCache(Deposit deposit) {
    storageCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putStorage(key, value);
        } else {
          getStorageStore().put(key.getData(), value.getStorage());
        }
      }
    }));
  }

  private void commitVoteCache(Deposit deposit) {
    votesCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putVotes(key, value);
        } else {
          getVotesStore().put(key.getData(), value.getVotes());
        }
      }
    }));
  }

  private void commitAccountContractIndex(Deposit deposit) {
    accountContractIndexCache.forEach(((key, value) -> {
      if (value.getType().isDirty() || value.getType().isCreate()) {
        if (deposit != null) {
          deposit.putContractByNormalAccountIndex(key, value);
        } else {
          getAccountContractIndexStore().put(key.getData(), value.getBytes());
        }
      }
    }));
  }

  @Override
  public void syncCacheFromAccountStore(byte[] address) {
    Key key = Key.create(address);
    int type;
    if (null == accounCache.get(key)) {
      type = Type.VALUE_TYPE_DIRTY;
    } else {
      type = Type.VALUE_TYPE_DIRTY | accounCache.get(key).getType().getType();
    }
    Value V = Value.create(getAccountStore().get(address).getData(), type);
    accounCache.put(key, V);
  }

  @Override
  public void syncCacheFromVotesStore(byte[] address) {
    Key key = Key.create(address);
    int type;
    if (null == votesCache.get(key)) {
      type = Type.VALUE_TYPE_DIRTY;
    } else {
      type = Type.VALUE_TYPE_DIRTY | votesCache.get(key).getType().getType();
    }
    Value V = Value.create(getVotesStore().get(address).getData(), type);
    votesCache.put(key, V);
  }

  @Override
  public synchronized void commit() {
    Deposit deposit = null;
    if (parent != null) {
      deposit = parent;
    } else if (prevDeposit != null) {
      deposit = prevDeposit;
    }

    commitAccountCache(deposit);
    commitTransactionCache(deposit);
    commitBlockCache(deposit);
    commitWitnessCache(deposit);
    commitCodeCache(deposit);
    commitContractCache(deposit);
    commitStorageCache(deposit);
    commitVoteCache(deposit);

  }

  @Override
  public void flush() {
    throw new RuntimeException("Not supported");
  }

  @Override
  public void setParent(Deposit deposit) {
    parent = deposit;
  }

  @Override
  public void setPrevDeposit(Deposit deposit) {
    prevDeposit = deposit;
  }

  @Override
  public void setNextDeposit(Deposit deposit) {
    nextDeposit = deposit;
  }

  public static DepositImpl createRoot(Manager dbManager) {
    return new DepositImpl(dbManager, null, null);
  }
}

package org.tron.common.storage;

import com.google.protobuf.ByteString;
import org.tron.common.runtime.vm.DataWord;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.capsule.StorageCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.protos.Protocol;

/**
 * @author Guo Yonggang
 * @since 2018.04
 */
public interface Deposit {

  Manager getDbManager();

  AccountCapsule createAccount(byte[] address, Protocol.AccountType type);

  AccountCapsule createAccount(byte[] address, ByteString accountName, Protocol.AccountType type);

  AccountCapsule getAccount(byte[] address);

  void createContract(byte[] address, ContractCapsule contractCapsule);

  void createContractByNormalAccountIndex(byte[] address, BytesCapsule contractAddress);

  ContractCapsule getContract(byte[] address);

  void saveCode(byte[] codeHash, byte[] code);

  byte[] getCode(byte[] codeHash);

  //byte[] getCodeHash(byte[] address);

  void addStorageValue(byte[] address, DataWord key, DataWord value);

  DataWord getStorageValue(byte[] address, DataWord key);

  StorageCapsule getStorage(byte[] address);

  long getBalance(byte[] address);

  long addBalance(byte[] address, long value) throws ContractExeException;


  Deposit newDepositChild();

  Deposit newDepositNext();

  void setParent(Deposit deposit);

  void setPrevDeposit(Deposit deposit);

  void setNextDeposit(Deposit deposit);

  void flush();

  void commit();

  void putAccount(Key key, Value value);

  void putTransaction(Key key, Value value);

  void putBlock(Key key, Value value);

  void putWitness(Key key, Value value);

  void putCode(Key key, Value value);

  void putContract(Key key, Value value);

  void putContractByNormalAccountIndex(Key key, Value value);

  void putStorage(Key key, Value value);

  void putVotes(Key key, Value value);

  void syncCacheFromAccountStore(byte[] address);

  void syncCacheFromVotesStore(byte[] address);

  TransactionCapsule getTransaction(byte[] trxHash);

  BlockCapsule getBlock(byte[] blockHash);

  BytesCapsule getContractByNormalAccount(byte[] address);

  long computeAfterRunStorageSize();

  long getBeforeRunStorageSize();

}

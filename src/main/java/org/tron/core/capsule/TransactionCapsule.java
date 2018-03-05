/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.ECKey.ECDSASignature;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.utils.TxInputUtil;
import org.tron.core.capsule.utils.TxOutputUtil;
import org.tron.core.db.UtxoStore;
import org.tron.protos.Protocal.Account;
import org.tron.protos.Protocal.TXInput;
import org.tron.protos.Protocal.TXOutput;
import org.tron.protos.Protocal.Transaction;
import org.tron.protos.Protocal.Transaction.Contract.ContractType;

public class TransactionCapsule {

  private static final Logger logger = LoggerFactory.getLogger("Transaction");

  private Transaction transaction;

  /**
   * constructor TransactionCapsule.
   */
  public TransactionCapsule(Transaction trx) {
    this.transaction = trx;
  }

  public TransactionCapsule(String key, int value) {
    TXInput.raw rawData = TXInput.raw.newBuilder()
        .setTxID(ByteString.copyFrom(new byte[]{}))
        .setVout(-1).build();

    TXInput txi = TXInput.newBuilder()
        .setSignature(ByteString.copyFrom(new byte[]{}))
        .setRawData(rawData).build();

    TXOutput txo = TXOutput.newBuilder()
        .setValue(value)
        .setPubKeyHash(ByteString.copyFrom(ByteArray.fromHexString(key)))
        .build();

    Transaction.raw.Builder rawCoinbaseTransaction = Transaction.raw.newBuilder()
        .addVin(txi)
        .addVout(txo);
    this.transaction = Transaction.newBuilder().setRawData(rawCoinbaseTransaction.build()).build();

  }

  /**
   * constructor TransactionCapsule.
   */
  public TransactionCapsule(
      byte[] address,
      String to,
      long amount,
      long balance,
      UtxoStore utxoStore
  ) {

    Transaction.raw.Builder transactionBuilder = Transaction.raw.newBuilder().addContract(
        Transaction.Contract.newBuilder().setType(ContractType.TransferContract).build());
    List<TXInput> txInputs = new ArrayList<>();
    List<TXOutput> txOutputs = new ArrayList<>();
    long spendableOutputs = balance;

    Set<Entry<String, long[]>> entrySet = utxoStore.findSpendableOutputs(address, amount)
        .getUnspentOutputs().entrySet();
    for (Map.Entry<String, long[]> entry : entrySet) {
      String txId = entry.getKey();
      long[] outs = entry.getValue();
      for (long out : outs) {
        TXInput txInput = TxInputUtil
            .newTxInput(ByteArray.fromHexString(txId), out, null, address);
        txInputs.add(txInput);
      }
    }

    txOutputs.add(TxOutputUtil.newTxOutput(amount, to));
    txOutputs
        .add(
            TxOutputUtil.newTxOutput(spendableOutputs - amount, ByteArray.toHexString(address)));

    if (checkBalance(address, to, amount, balance)) {
      for (TXInput txInput : txInputs) {
        transactionBuilder.addVin(txInput);
      }
      for (TXOutput txOutput : txOutputs) {
        transactionBuilder.addVout(txOutput);
      }
      logger.info("Transaction create succeeded！");
      transaction = Transaction.newBuilder().setRawData(transactionBuilder.build()).build();
    } else {
      logger.error("Transaction create failed！");
      transaction = null;
    }
  }

  // TODO
  public TransactionCapsule(byte[] address, Account account) {
    Transaction.raw.Builder transactionBuilder = Transaction.raw.newBuilder()
        .addContract(
            Transaction.Contract.newBuilder().setType(ContractType.AccountCreateContract).build());
    //.setParameter(Any.pack(account));
  }

  public Sha256Hash getHash() {
    byte[] transBytes = this.transaction.toByteArray();
    return Sha256Hash.of(transBytes);
  }

  public Sha256Hash getRawHash() {
    return Sha256Hash.of(this.transaction.getRawData().toByteArray());
  }

  /**
   * cheack balance of the address.
   */
  public boolean checkBalance(byte[] address, String to, long amount, long balance) {

    if (to.length() != 40) {
      logger.error("address invalid");
      return false;
    }

    if (amount <= 0) {
      logger.error("amount required a positive number");
      return false;
    }

    if (amount > balance) {
      logger.error("don't have enough money");
      return false;
    }

    return true;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void sign(byte[] privateKey) {
    ECKey ecKey = ECKey.fromPrivate(privateKey);
    ECDSASignature signature = ecKey.sign(getRawHash().getBytes());
    ByteString sig = ByteString.copyFrom(signature.toByteArray());
    this.transaction = this.transaction.toBuilder().addSignature(sig).build();
  }


  /**
   * TODO
   * validateSignature.
   */
  public boolean validateSignature() {
    return true;
  }


  public Sha256Hash getTransactionId() {
    return Sha256Hash.of(this.transaction.toByteArray());
  }

  public byte[] getData() {
    return this.transaction.toByteArray();
  }

  @Override
  public String toString() {
    return this.transaction.toString();
  }
}

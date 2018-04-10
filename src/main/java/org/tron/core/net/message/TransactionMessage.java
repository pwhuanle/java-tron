package org.tron.core.net.message;

import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.protos.Protocol.Transaction;

public class TransactionMessage extends TronMessage {

  private Transaction trx;

  public TransactionMessage(byte[] packed) {
    super(packed);
    this.type = MessageTypes.TRX.asByte();
  }

  public TransactionMessage(Transaction trx) {
    this.trx = trx;
    unpacked = true;
    this.type = MessageTypes.TRX.asByte();
  }

  @Override
  public MessageTypes getType() {
    return MessageTypes.fromByte(this.type);
  }

  @Override
  public byte[] getData() {
    if (data == null) {
      pack();
    }
    return data;
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  public Transaction getTransaction() {
    unPack();
    return trx;
  }

  public TransactionCapsule getTransactionCapsule() {
    return new TransactionCapsule(getTransaction());
  }

  private synchronized void unPack() {
    if (unpacked) {
      return;
    }

    try {
      this.trx = Transaction.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }

    unpacked = true;
  }

  private void pack() {
    this.data = this.trx.toByteArray();
  }

}

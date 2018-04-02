package org.tron.core.net.message;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.Sha256Hash;
import org.tron.protos.Protocol.Inventory;
import org.tron.protos.Protocol.Inventory.InventoryType;

@Slf4j(topic = "core.net")
public class InventoryMessage extends TronMessage {

  protected Inventory inv;

  public InventoryMessage(byte[] packed) {
    super(packed);
    this.type = MessageTypes.INVENTORY.asByte();
  }

  public InventoryMessage() {
    this.type = MessageTypes.INVENTORY.asByte();
  }

  public InventoryMessage(Inventory inv) {
    this.inv = inv;
    unpacked = true;
    this.type = MessageTypes.INVENTORY.asByte();
  }

  public InventoryMessage(List<Sha256Hash> hashList, InventoryType type) {
    Inventory.Builder invBuilder = Inventory.newBuilder();

    for (Sha256Hash hash :
        hashList) {
      invBuilder.addIds(hash.getByteString());
    }
    invBuilder.setType(type);
    inv = invBuilder.build();
    unpacked = true;
    this.type = MessageTypes.INVENTORY.asByte();
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

  @Override
  public MessageTypes getType() {
    return MessageTypes.fromByte(this.type);
  }

  public Inventory getInventory() {
    unPack();
    return inv;
  }

  public MessageTypes getInvMessageType() {
    return getInventoryType().equals(InventoryType.BLOCK) ? MessageTypes.BLOCK : MessageTypes.TRX;

  }

  public InventoryType getInventoryType() {
    unPack();
    return inv.getType();
  }

  private synchronized void unPack() {
    if (unpacked) {
      return;
    }

    try {
      this.inv = Inventory.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }

    unpacked = true;
  }

  public List<Sha256Hash> getHashList() {
    return getInventory().getIdsList().stream()
        .map(hash -> Sha256Hash.wrap(hash.toByteArray()))
        .collect(Collectors.toList());
  }

  private void pack() {
    this.data = this.inv.toByteArray();
  }
}

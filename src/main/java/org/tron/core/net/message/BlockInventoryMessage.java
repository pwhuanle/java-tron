package org.tron.core.net.message;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BlockCapsule.BlockId;
import org.tron.protos.Protocol.BlockInventory;

public class BlockInventoryMessage extends TronMessage {

  protected BlockInventory blockInventory;

  public BlockInventoryMessage(byte[] packed) {
    super(packed);
    this.type = MessageTypes.BLOCK_INVENTORY.asByte();
  }

  @Override
  public Sha256Hash getMessageId() {
    return super.getMessageId();
  }

  @Override
  public byte[] getData() {
    if (data == null) {
      pack();
    }
    return data;
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  private BlockInventory getBlockInventory() {
    unPack();
    return blockInventory;
  }

  private void pack() {
    this.data = this.blockInventory.toByteArray();
  }

  private synchronized void unPack() {
    if (unpacked) {
      return;
    }

    try {
      this.blockInventory = BlockInventory.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }
    unpacked = true;
  }

  public BlockInventoryMessage(List<BlockId> blockIds, BlockInventory.Type type) {
    BlockInventory.Builder invBuilder = BlockInventory.newBuilder();
    blockIds.forEach(blockId -> {
      BlockInventory.BlockId.Builder b = BlockInventory.BlockId.newBuilder();
      b.setHash(blockId.getByteString());
      b.setNumber(blockId.getNum());
      invBuilder.addIds(b);
    });

    invBuilder.setType(type);
    blockInventory = invBuilder.build();
    this.type = MessageTypes.BLOCK_INVENTORY.asByte();
    unpacked = true;
  }

  public List<BlockId> getBlockIds() {
    return getBlockInventory().getIdsList().stream()
        .map(blockId -> new BlockId(blockId.getHash(), blockId.getNumber()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public MessageTypes getType() {
    return MessageTypes.fromByte(this.type);
  }

  //public List<BlockId> getBlockIds()
}

package org.tron.core.net.message;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BlockCapsule.BlockId;
import org.tron.protos.Protocol.ChainInventory;

@Slf4j(topic = "core.net")
public class ChainInventoryMessage extends TronMessage {

  public ChainInventoryMessage(byte[] packed) {
    super(packed);
    this.type = MessageTypes.BLOCK_CHAIN_INVENTORY.asByte();
  }

  protected ChainInventory chainInventory;

  public ChainInventoryMessage(List<BlockId> blockIds, Long remainNum) {
    ChainInventory.Builder invBuilder = ChainInventory.newBuilder();
    blockIds.forEach(blockId -> {
      ChainInventory.BlockId.Builder b = ChainInventory.BlockId.newBuilder();
      b.setHash(blockId.getByteString());
      b.setNumber(blockId.getNum());
      invBuilder.addIds(b);
    });

    invBuilder.setRemainNum(remainNum);
    chainInventory = invBuilder.build();
    unpacked = true;
    this.type = MessageTypes.BLOCK_CHAIN_INVENTORY.asByte();
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
  public Class<?> getAnswerMessage() {
    return null;
  }

  private void pack() {
    this.data = this.chainInventory.toByteArray();
  }

  private ChainInventory getChainInventory() {
    unPack();
    return chainInventory;
  }

  public List<BlockId> getBlockIds() {

    try {
      return getChainInventory().getIdsList().stream()
          .map(blockId -> new BlockId(blockId.getHash(), blockId.getNumber()))
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (Exception e) {
      logger.info("breakPoint");
    }
    return null;
  }

  public Long getRemainNum() {
    return getChainInventory().getRemainNum();
  }

  private synchronized void unPack() {
    if (unpacked) {
      return;
    }

    try {
      this.chainInventory = ChainInventory.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.info(e.getMessage());
    }
    unpacked = true;
  }


  @Override
  public MessageTypes getType() {
    return MessageTypes.fromByte(this.type);
  }
}

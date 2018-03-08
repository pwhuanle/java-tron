package org.tron.core.net.message;

import org.tron.protos.Protocol;

public class ItemNotFound extends Message {

  private org.tron.protos.Protocol.Items notFound;

  /**
   * means can not find this block or trx.
   */
  public ItemNotFound() {
    Protocol.Items.Builder itemsBuilder = Protocol.Items.newBuilder();
    itemsBuilder.setType(Protocol.Items.ItemType.ERR);
    notFound = itemsBuilder.build();
  }

  @Override
  public byte[] getData() {
    return notFound.toByteArray();
  }

  @Override
  public String toString() {
    return "item not found";
  }

  @Override
  public MessageTypes getType() {
    return MessageTypes.ITEM_NOT_FOUND;
  }
}

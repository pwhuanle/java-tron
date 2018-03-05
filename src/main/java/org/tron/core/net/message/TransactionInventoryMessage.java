package org.tron.core.net.message;

import java.util.List;
import org.tron.common.utils.Sha256Hash;
import org.tron.protos.Protocal.Inventory;
import org.tron.protos.Protocal.Inventory.InventoryType;

public class TransactionInventoryMessage extends InventoryMessage  {

  public TransactionInventoryMessage(byte[] packed) {
    super(packed);
  }

  public TransactionInventoryMessage(Inventory inv) {
    super(inv);
  }

  public TransactionInventoryMessage(List<Sha256Hash> hashList) {
    super(hashList, InventoryType.TRX);
  }
}

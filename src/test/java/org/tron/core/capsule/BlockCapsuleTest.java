package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Constant;
import org.tron.core.config.args.Args;
import org.tron.core.exception.BadItemException;

@Slf4j
public class BlockCapsuleTest {

  private static BlockCapsule blockCapsule0 = new BlockCapsule(1, ByteString
      .copyFrom(ByteArray
          .fromHexString("9938a342238077182498b464ac0292229938a342238077182498b464ac029222")), 1234,
      ByteString.copyFrom("1234567".getBytes()));
  private static String dbPath = "output_bloackcapsule_test";

  @BeforeClass
  public static void init() {
    Args.setParam(new String[]{"-d", dbPath},
        Constant.TEST_CONF);
  }

  @AfterClass
  public static void removeDb() {
    Args.clearParam();
    FileUtil.deleteDir(new File(dbPath));
  }

  @Test
  public void testCalcMerkleRoot() {
    blockCapsule0.setMerkleRoot();
    Assert.assertEquals(
        Sha256Hash.wrap(Sha256Hash.ZERO_HASH.getByteString()).toString(),
        blockCapsule0.getMerkleRoot().toString());

    logger.info("Transaction[X] Merkle Root : {}", blockCapsule0.getMerkleRoot().toString());

    TransactionCapsule transactionCapsule1 = new TransactionCapsule("123", 1L);
    TransactionCapsule transactionCapsule2 = new TransactionCapsule("124", 2L);
    blockCapsule0.addTransaction(transactionCapsule1);
    blockCapsule0.addTransaction(transactionCapsule2);
    blockCapsule0.setMerkleRoot();

    Assert.assertEquals(
        "e44ff03f9dacdeb0986d2b82e1bc028b32e1270d3f7e602f14d087993b2afcb5",
        blockCapsule0.getMerkleRoot().toString());

    logger.info("Transaction[O] Merkle Root : {}", blockCapsule0.getMerkleRoot().toString());
  }

  /* @Test
  public void testAddTransaction() {
    TransactionCapsule transactionCapsule = new TransactionCapsule("123", 1L);
    blockCapsule0.addTransaction(transactionCapsule);
    Assert.assertArrayEquals(blockCapsule0.getTransactions().get(0).getHash().getBytes(),
        transactionCapsule.getHash().getBytes());
    Assert.assertEquals(transactionCapsule.getInstance().getRawData().getVout(0).getValue(),
        blockCapsule0.getTransactions().get(0).getInstance().getRawData().getVout(0).getValue());
  } */

  @Test
  public void testGetData() {
    blockCapsule0.getData();
    byte[] b = blockCapsule0.getData();
    BlockCapsule blockCapsule1 = null;
    try {
      blockCapsule1 = new BlockCapsule(b);
      Assert.assertEquals(blockCapsule0.getBlockId(), blockCapsule1.getBlockId());
    } catch (BadItemException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testValidate() {

  }

  @Test
  public void testGetInsHash() {
    Assert.assertEquals(1,
        blockCapsule0.getInstance().getBlockHeader().getRawData().getNumber());
    Assert.assertEquals(blockCapsule0.getParentHash(),
        Sha256Hash.wrap(blockCapsule0.getParentHashStr()));
  }

  @Test
  public void testGetTimeStamp() {
    Assert.assertEquals(1234L, blockCapsule0.getTimeStamp());
  }

}
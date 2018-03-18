package org.tron.core.capsule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Constant;
import org.tron.core.config.Configuration;
import org.tron.core.config.args.Args;
import com.google.protobuf.ByteString;


public class BlockCapsuleTest {

  private static final Logger logger = LoggerFactory.getLogger("Test");
  private static BlockCapsule blockCapsule0 = new BlockCapsule(1, ByteString
      .copyFrom(ByteArray
          .fromHexString("9938a342238077182498b464ac0292229938a342238077182498b464ac029222")), 1234,
      ByteString.copyFrom("1234567".getBytes()));
  private static String dbPath = "output_bloackcapsule";

  @BeforeClass
  public static void init() {
    Args.setParam(new String[]{"-d", dbPath},
        Configuration.getByPath(Constant.TEST_CONF));
  }

  @AfterClass
  public static void removeDb() {
    FileUtil.recursiveDelete(dbPath);
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
            "fbf357d2f8c5db313e87bf0cb67dc69db4e11aef31bdfe6c2faa4519d91372a1",
            blockCapsule0.getMerkleRoot().toString());

    logger.info("Transaction[O] Merkle Root : {}", blockCapsule0.getMerkleRoot().toString());
  }

  @Test
  public void testAddTransaction() {
    TransactionCapsule transactionCapsule = new TransactionCapsule("123", 1L);
    blockCapsule0.addTransaction(transactionCapsule);
    Assert.assertArrayEquals(blockCapsule0.getTransactions().get(0).getHash().getBytes(),
        transactionCapsule.getHash().getBytes());
    Assert.assertEquals(transactionCapsule.getInstance().getRawData().getVout(0).getValue(),
        blockCapsule0.getTransactions().get(0).getInstance().getRawData().getVout(0).getValue());
  }

  @Test
  public void testGetData() {
    blockCapsule0.getData();
    byte[] b = blockCapsule0.getData();
    BlockCapsule blockCapsule1 = new BlockCapsule(b);
    Assert.assertEquals(blockCapsule0.getBlockId(), blockCapsule1.getBlockId());
  }

  @Test
  public void testValidate() {
    Assert.assertTrue(blockCapsule0.validate());
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
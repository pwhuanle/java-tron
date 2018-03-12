package org.tron.core.db;

import com.google.protobuf.ByteString;
import java.io.File;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Constant;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.Configuration;
import org.tron.core.config.args.Args;


public class ManagerTest {

  private static final Logger logger = LoggerFactory.getLogger("Test");
  private static Manager dbManager = new Manager();
  private static BlockCapsule blockCapsule2;
  private static String dbPath = "output_manager";

  @BeforeClass
  public static void init() {
    Args.setParam(new String[]{"-d", dbPath},
        Configuration.getByPath(Constant.TEST_CONF));

    dbManager.init();
    blockCapsule2 = new BlockCapsule(1, ByteString.copyFrom(ByteArray
        .fromHexString("0304f784e4e7bae517bcab94c3e0c9214fb4ac7ff9d7d5a937d1f40031f87b81")),
        0,
        ByteString.copyFrom(
            ECKey.fromPrivate(ByteArray.fromHexString(Args.getInstance().getPrivateKey()))
                .getAddress()));
    blockCapsule2.setMerklerRoot();
    blockCapsule2.sign(Args.getInstance().getPrivateKey().getBytes());
  }

  @AfterClass
  public static void removeDb() {
    File dbFolder = new File(dbPath);
    deleteFolder(dbFolder);
  }

  private static void deleteFolder(File index) {
    if (!index.isDirectory() || index.listFiles().length <= 0) {
      index.delete();
      return;
    }
    for (File file : index.listFiles()) {
      if (null != file) {
        deleteFolder(file);
      }
    }
    index.delete();
  }

  @Test
  public void pushBlock() {
    try {
      dbManager.pushBlock(blockCapsule2);
    } catch (Exception e) {
      Assert.assertTrue("pushBlock is error", false);
    }

    Assert.assertTrue("containBlock is error", dbManager.containBlock(Sha256Hash.wrap(ByteArray
        .fromHexString(blockCapsule2.getBlockId().toString()))));
    Assert.assertEquals("getBlockIdByNum is error", blockCapsule2.getBlockId().toString(),
        dbManager.getBlockIdByNum(1).toString());
    Assert.assertTrue("hasBlocks is error", dbManager.hasBlocks());

    dbManager.deleteBlock(Sha256Hash.wrap(ByteArray
        .fromHexString(blockCapsule2.getBlockId().toString())));

    Assert.assertFalse("deleteBlock is error", dbManager.containBlock(Sha256Hash.wrap(ByteArray
        .fromHexString(blockCapsule2.getBlockId().toString()))));
}

  @Test
  public void testPushTransactions() {
    TransactionCapsule transactionCapsule = new TransactionCapsule(
        "2c0937534dd1b3832d05d865e8e6f2bf23218300b33a992740d45ccab7d4f519", 123);
    try {
      dbManager.pushTransactions(transactionCapsule);
    } catch (Exception e) {
      Assert.assertTrue("pushTransaction is error", false);
    }
    Assert.assertEquals("pushTransaction is error", 123,
        transactionCapsule.getInstance().getRawData().getVout(0).getValue());
  }
}

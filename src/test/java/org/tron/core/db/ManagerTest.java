package org.tron.core.db;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.common.utils.Sha256Hash;
import org.tron.common.utils.Utils;
import org.tron.core.Constant;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.capsule.WitnessCapsule;
import org.tron.core.config.args.Args;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.core.exception.UnLinkedBlockException;
import org.tron.core.exception.ValidateScheduleException;
import org.tron.core.exception.ValidateSignatureException;
import org.tron.core.witness.WitnessController;
import org.tron.protos.Contract.TransferContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;

@Slf4j
public class ManagerTest {

  private static Manager dbManager = new Manager();
  private static BlockCapsule blockCapsule2;
  private static String dbPath = "output_manager_test";

  @BeforeClass
  public static void init() {
    Args.setParam(new String[]{"-d", dbPath, "-w"},
        Constant.TEST_CONF);

    dbManager.init();

    blockCapsule2 = new BlockCapsule(1, ByteString.copyFrom(ByteArray
        .fromHexString("0304f784e4e7bae517bcab94c3e0c9214fb4ac7ff9d7d5a937d1f40031f87b81")),
        0,
        ByteString.copyFrom(
            ECKey.fromPrivate(ByteArray
                .fromHexString(Args.getInstance().getLocalWitnesses().getPrivateKey()))
                .getAddress()));
    blockCapsule2.setMerkleRoot();
    blockCapsule2.sign(
        ByteArray.fromHexString(Args.getInstance().getLocalWitnesses().getPrivateKey()));

  }

  @AfterClass
  public static void removeDb() {
    Args.clearParam();
    FileUtil.deleteDir(new File(dbPath));
    dbManager.destroy();
  }

  @Test
  public void setBlockReference()
      throws ContractExeException, UnLinkedBlockException, ValidateScheduleException, ContractValidateException, ValidateSignatureException {

    BlockCapsule blockCapsule = new BlockCapsule(1, dbManager.getGenesisBlockId().getByteString(),
        0,
        ByteString.copyFrom(
            ECKey.fromPrivate(ByteArray
                .fromHexString(Args.getInstance().getLocalWitnesses().getPrivateKey()))
                .getAddress()));
    blockCapsule.setMerkleRoot();
    blockCapsule.sign(
        ByteArray.fromHexString(Args.getInstance().getLocalWitnesses().getPrivateKey()));

    TransferContract tc = TransferContract.newBuilder().setAmount(10)
        .setOwnerAddress(ByteString.copyFromUtf8("aaa"))
        .setToAddress(ByteString.copyFromUtf8("bbb")).build();
    TransactionCapsule trx = new TransactionCapsule(tc, ContractType.TransferContract);

    if (dbManager.getDynamicPropertiesStore().getLatestBlockHeaderNumber() == 0) {
      dbManager.pushBlock(blockCapsule);
      Assert.assertEquals(1, dbManager.getDynamicPropertiesStore().getLatestBlockHeaderNumber());
      dbManager.setBlockReference(trx);
      Assert.assertEquals(1, trx.getInstance().getRawData().getRefBlockNum());
    }
    while (dbManager.getDynamicPropertiesStore().getLatestBlockHeaderNumber() > 0) {
      try {
        dbManager.eraseBlock();
      } catch (BadItemException e) {
        e.printStackTrace();
      } catch (ItemNotFoundException e) {
        e.printStackTrace();
      }
    }
    try {
      dbManager.pushBlock(blockCapsule);
      Assert.assertEquals(1, dbManager.getDynamicPropertiesStore().getLatestBlockHeaderNumber());
    } catch (ValidateSignatureException e) {
      e.printStackTrace();
    } catch (ContractValidateException e) {
      e.printStackTrace();
    } catch (ContractExeException e) {
      e.printStackTrace();
    } catch (UnLinkedBlockException e) {
      e.printStackTrace();
    } catch (ValidateScheduleException e) {
      e.printStackTrace();
    }
    Assert.assertEquals(1, dbManager.getDynamicPropertiesStore().getLatestBlockHeaderNumber());
    dbManager.setBlockReference(trx);
    Assert.assertEquals(1, trx.getInstance().getRawData().getRefBlockNum());

  }

  @Test
  public void pushBlock() {
    boolean isUnlinked = false;
    try {
      dbManager.pushBlock(blockCapsule2);
    } catch (UnLinkedBlockException e) {
      isUnlinked = true;
    } catch (Exception e) {
      Assert.assertTrue("pushBlock is error", false);
    }

    Assert.assertTrue("containBlock is error", dbManager.containBlock(Sha256Hash.wrap(ByteArray
        .fromHexString(blockCapsule2.getBlockId().toString()))));

    if (isUnlinked) {
      Assert.assertEquals("getBlockIdByNum is error", dbManager.getHeadBlockNum(),
          0);
    } else {
      try {
        Assert.assertEquals("getBlockIdByNum is error", blockCapsule2.getBlockId().toString(),
            dbManager.getBlockIdByNum(1).toString());
      } catch (BadItemException e) {
        e.printStackTrace();
      } catch (ItemNotFoundException e) {
        e.printStackTrace();
      }
    }

    Assert.assertTrue("hasBlocks is error", dbManager.hasBlocks());

  }


  //    @Test
  public void updateWits() {
    int sizePrv = dbManager.getWitnesses().size();
    dbManager.getWitnesses().forEach(witnessCapsule -> {
      logger.info("witness address is {}",
          ByteArray.toHexString(witnessCapsule.getAddress().toByteArray()));
    });
    logger.info("------------");
    WitnessCapsule witnessCapsulef = new WitnessCapsule(
        ByteString.copyFrom(ByteArray.fromHexString("0x0011")), "www.tron.net/first");
    witnessCapsulef.setIsJobs(true);
    WitnessCapsule witnessCapsules = new WitnessCapsule(
        ByteString.copyFrom(ByteArray.fromHexString("0x0012")), "www.tron.net/second");
    witnessCapsules.setIsJobs(true);
    WitnessCapsule witnessCapsulet = new WitnessCapsule(
        ByteString.copyFrom(ByteArray.fromHexString("0x0013")), "www.tron.net/three");
    witnessCapsulet.setIsJobs(false);

    dbManager.getWitnesses().forEach(witnessCapsule -> {
      logger.info("witness address is {}",
          ByteArray.toHexString(witnessCapsule.getAddress().toByteArray()));
    });
    logger.info("---------");
    dbManager.getWitnessStore().put(witnessCapsulef.getAddress().toByteArray(), witnessCapsulef);
    dbManager.getWitnessStore().put(witnessCapsules.getAddress().toByteArray(), witnessCapsules);
    dbManager.getWitnessStore().put(witnessCapsulet.getAddress().toByteArray(), witnessCapsulet);
    dbManager.getWitnessController().initWits();
    dbManager.getWitnesses().forEach(witnessCapsule -> {
      logger.info("witness address is {}",
          ByteArray.toHexString(witnessCapsule.getAddress().toByteArray()));
    });
    int sizeTis = dbManager.getWitnesses().size();
    Assert.assertEquals("update add witness size is ", 2, sizeTis - sizePrv);
  }

  @Test
  public void fork() throws ValidateSignatureException,
      ContractValidateException,
      ContractExeException,
      UnLinkedBlockException,
      ValidateScheduleException,
      BadItemException,
      ItemNotFoundException {
    Args.setParam(new String[]{"--witness"}, Constant.TEST_CONF);
    long size = dbManager.getBlockStore().dbSource.allKeys().size();

    Map<ByteString, String> addressToProvateKeys = addTestWitnessAndAccount();

    long num = dbManager.getDynamicPropertiesStore().getLatestBlockHeaderNumber();
    BlockCapsule blockCapsule0 = createTestBlockCapsule(num + 1,
        dbManager.getDynamicPropertiesStore().getLatestBlockHeaderHash().getByteString(),
        addressToProvateKeys);

    BlockCapsule blockCapsule1 = createTestBlockCapsule(num + 1,
        dbManager.getDynamicPropertiesStore().getLatestBlockHeaderHash().getByteString(),
        addressToProvateKeys);

    BlockCapsule blockCapsule2 = createTestBlockCapsule(num + 2,
        blockCapsule1.getBlockId().getByteString(), addressToProvateKeys);

    dbManager.pushBlock(blockCapsule0);
    dbManager.pushBlock(blockCapsule1);
    dbManager.pushBlock(blockCapsule2);

    Assert.assertNotNull(dbManager.getBlockStore().get(blockCapsule1.getBlockId().getBytes()));
    Assert.assertNotNull(dbManager.getBlockStore().get(blockCapsule2.getBlockId().getBytes()));

    Assert.assertEquals(
        dbManager.getBlockStore().get(blockCapsule2.getBlockId().getBytes()).getParentHash(),
        blockCapsule1.getBlockId());

    Assert.assertEquals(dbManager.getBlockStore().dbSource.allKeys().size(), size + 2);

    Assert.assertEquals(dbManager.getBlockIdByNum(dbManager.getHead().getNum() - 1),
        blockCapsule1.getBlockId());
    Assert.assertEquals(dbManager.getBlockIdByNum(dbManager.getHead().getNum() - 2),
        blockCapsule1.getParentHash());

    Assert.assertEquals(blockCapsule2.getBlockId(),
        dbManager.getDynamicPropertiesStore().getLatestBlockHeaderHash());
    Assert.assertEquals(dbManager.getHead().getBlockId(),
        dbManager.getDynamicPropertiesStore().getLatestBlockHeaderHash());
  }

  private Map<ByteString, String> addTestWitnessAndAccount() {
    dbManager.getWitnesses().clear();
    return IntStream.range(0, 2).mapToObj(i -> {
      ECKey ecKey = new ECKey(Utils.getRandom());
      String privateKey = ByteArray.toHexString(ecKey.getPrivKey().toByteArray());
      ByteString address = ByteString.copyFrom(ecKey.getAddress());

      WitnessCapsule witnessCapsule = new WitnessCapsule(address);
      dbManager.getWitnessStore().put(address.toByteArray(), witnessCapsule);
      dbManager.getWitnessController().addWitness(witnessCapsule);

      AccountCapsule accountCapsule =
          new AccountCapsule(Account.newBuilder().setAddress(address).build());
      dbManager.getAccountStore().put(address.toByteArray(), accountCapsule);

      return Maps.immutableEntry(address, privateKey);
    })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private BlockCapsule createTestBlockCapsule(long number, ByteString hash,
      Map<ByteString, String> addressToProvateKeys) {
    long time = System.currentTimeMillis();
    WitnessController witnessController = dbManager.getWitnessController();
    ByteString witnessAddress =
        witnessController.getScheduledWitness(witnessController.getSlotAtTime(time));
    BlockCapsule blockCapsule = new BlockCapsule(number, hash, time, witnessAddress);
    blockCapsule.generatedByMyself = true;
    blockCapsule.setMerkleRoot();
    blockCapsule.sign(ByteArray.fromHexString(addressToProvateKeys.get(witnessAddress)));
    return blockCapsule;
  }

}

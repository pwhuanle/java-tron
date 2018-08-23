package stest.tron.wallet.account;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class WalletTestAccount008 {
  private final String testKey002 =
      "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";

  private final byte[] fromAddress = PublicMethed.getFinalAddress(testKey002);

  private static final long now = System.currentTimeMillis();
  private static String name = "AssetIssue012_" + Long.toString(now);
  private static final long totalSupply = now;
  private static final long sendAmount = 10000000000L;

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);

  private static final long FREENETLIMIT = 5000L;
  private static final long BASELINE = 4800L;

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] account008Address = ecKey1.getAddress();
  String account008Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] account008SecondAddress = ecKey2.getAddress();
  String account008SecondKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

  ECKey ecKey3 = new ECKey(Utils.getRandom());
  byte[] account008InvalidAddress = ecKey3.getAddress();
  String account008InvalidKey = ByteArray.toHexString(ecKey3.getPrivKeyBytes());


  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass(enabled = true)
  public void beforeClass() {
    PublicMethed.printAddress(account008Key);
    PublicMethed.printAddress(account008SecondKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    Assert.assertTrue(PublicMethed.sendcoin(account008Address,10000000,
        fromAddress,testKey002,blockingStubFull));
    Assert.assertTrue(PublicMethed.sendcoin(account008SecondAddress,10000000,
        fromAddress,testKey002,blockingStubFull));
    Assert.assertTrue(PublicMethed.sendcoin(account008InvalidAddress,10000000,
        fromAddress,testKey002,blockingStubFull));

  }

  @Test(enabled = true)
  public void testSetAccountId() {
    String lessThan7Char = getRandomStr(7);
    String moreThan32Char = getRandomStr(33);
    String shortAccountId = getRandomStr(8);
    String longAccountId = getRandomStr(32);
    //Less than 7 char can't set success.
    Assert.assertFalse(PublicMethed.setAccountId(lessThan7Char.getBytes(),account008Address,
        account008Key,blockingStubFull));
    //More than 33 char can't set success.
    Assert.assertFalse(PublicMethed.setAccountId(moreThan32Char.getBytes(),account008Address,
        account008Key,blockingStubFull));
    //The shortest char is 8,it can success.
    Assert.assertTrue(PublicMethed.setAccountId(shortAccountId.getBytes(),account008Address,

        account008Key,blockingStubFull));
    //One account only can set account id 1 time.
    Assert.assertFalse(PublicMethed.setAccountId(longAccountId.getBytes(),account008Address,
        account008Key,blockingStubFull));
    //One account id only can set by one account, when another account try to set, is will failed.
    Assert.assertFalse(PublicMethed.setAccountId(shortAccountId.getBytes(),account008SecondAddress,
        account008SecondKey,blockingStubFull));
    //The longest char is 32, it can success.
    Assert.assertTrue(PublicMethed.setAccountId(longAccountId.getBytes(),account008SecondAddress,
        account008SecondKey,blockingStubFull));

    Account account008Info = PublicMethed.queryAccount(account008Key,blockingStubFull);
    Assert.assertTrue(ByteArray.toStr(account008Info.getAccountId().toByteArray()).length() == 8);
    Account account008SecondInfo = PublicMethed.queryAccount(account008SecondKey,blockingStubFull);
    Assert.assertTrue(ByteArray.toStr(account008SecondInfo.getAccountId()
        .toByteArray()).length() == 32);

  }

  @Test(enabled = true)
  public void testSetInvalidAccountId() {
    String hasSpaceAccountId = getRandomStr(4) + " " + getRandomStr(10);
    String hasChineseAccountId = getRandomStr(4) + "中文账户名称";
    Assert.assertFalse(PublicMethed.setAccountId(hasSpaceAccountId.getBytes(),
        account008InvalidAddress, account008InvalidKey,blockingStubFull));
    Assert.assertFalse(PublicMethed.setAccountId(hasChineseAccountId.getBytes(),
        account008InvalidAddress, account008InvalidKey,blockingStubFull));


  }

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }


  public static String getRandomStr(int length) {
    String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    int randomNum;
    char randomChar;
    Random random = new Random();
    // StringBuffer类型的可以append增加字符
    StringBuffer str = new StringBuffer();

    for (int i = 0; i < length; i++) {
      // 可生成[0,n)之间的整数，获得随机位置
      randomNum = random.nextInt(base.length());
      // 获得随机位置对应的字符
      randomChar = base.charAt(randomNum);
      // 组成一个随机字符串
      str.append(randomChar);
    }
    return str.toString();
  }

}



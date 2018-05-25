package stest.tron.wallet.Wallettest_p1;

import com.google.protobuf.ByteString;
//import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.utils.Base58;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

//import stest.tron.wallet.common.client.AccountComparator;

@Slf4j
public class WalletTest_p1_Block_002 {

    //testng001、testng002、testng003、testng004
    private final static  String testKey001     = "8CB4480194192F30907E14B52498F594BD046E21D7C4D8FE866563A6760AC891";
    private final static  String testKey002     = "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
    private final static  String testKey003     = "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";
    private final static  String testKey004     = "592BB6C9BB255409A6A43EFD18E6A74FECDDCCE93A40D96B70FBE334E6361E32";

    //testng001、testng002、testng003、testng004
    private static final byte[] BACK_ADDRESS    = Base58.decodeFromBase58Check("27YcHNYcxHGRf5aujYzWQaJSpQ4WN4fJkiU");
    private static final byte[] FROM_ADDRESS    = Base58.decodeFromBase58Check("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv");
    private static final byte[] TO_ADDRESS      = Base58.decodeFromBase58Check("27iDPGt91DX3ybXtExHaYvrgDt5q5d6EtFM");
    private static final byte[] NEED_CR_ADDRESS = Base58.decodeFromBase58Check("27QEkeaPHhUSQkw9XbxX3kCKg684eC2w67T");

    private ManagedChannel channelFull = null;
    private WalletGrpc.WalletBlockingStub blockingStubFull = null;
    //private String fullnode = "39.105.111.178:50051";
    private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list").get(0);

    @BeforeClass
    public void beforeClass(){
        channelFull = ManagedChannelBuilder.forTarget(fullnode)
                .usePlaintext(true)
                .build();
        blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    }


    @Test(enabled = true)
    public void TestGetBlockByNum(){
        //获取当前区块number，如果当前区块number为0，则打印日志，转告测试手动测试该用例或等待有新区块后再测试
        Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
        Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
        Assert.assertFalse(currentBlockNum < 0);
        if (currentBlockNum == 1){
            logger.info("Now has very little block, Please test this case by manual");
            Assert.assertTrue(currentBlockNum == 1);
        }

        //定义一个远远大于当前区块数量都值，使用该值查询区块信息，设备无异常，查询不到区块头信息
        Long outOfCurrentBlockNum = currentBlockNum + 10000L;
        NumberMessage.Builder builder1 = NumberMessage.newBuilder();
        builder1.setNum(outOfCurrentBlockNum);
        Block outOfCurrentBlock = blockingStubFull.getBlockByNum(builder1.build());
        Assert.assertFalse(outOfCurrentBlock.hasBlockHeader());

        //查询第一个区块信息
        NumberMessage.Builder builder2 = NumberMessage.newBuilder();
        builder2.setNum(1);
        Block firstBlock = blockingStubFull.getBlockByNum(builder2.build());
        Assert.assertTrue(firstBlock.hasBlockHeader());
        Assert.assertFalse(firstBlock.getBlockHeader().getWitnessSignature().isEmpty());
        Assert.assertTrue(firstBlock.getBlockHeader().getRawData().getTimestamp() > 0);
        Assert.assertFalse(firstBlock.getBlockHeader().getRawData().getWitnessAddress().isEmpty());
        Assert.assertTrue(firstBlock.getBlockHeader().getRawData().getNumber() == 1);
        Assert.assertFalse(firstBlock.getBlockHeader().getRawData().getParentHash().isEmpty());
        Assert.assertTrue(firstBlock.getBlockHeader().getRawData().getWitnessId() >= 0);
        //logger.info("firstblock test succesfully");

        //查询当前区块前一个区块信息
        NumberMessage.Builder builder3 = NumberMessage.newBuilder();
        builder3.setNum(currentBlockNum -1);
        Block lastSecondBlock = blockingStubFull.getBlockByNum(builder3.build());
        Assert.assertTrue(lastSecondBlock.hasBlockHeader());
        Assert.assertFalse(lastSecondBlock.getBlockHeader().getWitnessSignature().isEmpty());
        Assert.assertTrue(lastSecondBlock.getBlockHeader().getRawData().getTimestamp() > 0);
        Assert.assertFalse(lastSecondBlock.getBlockHeader().getRawData().getWitnessAddress().isEmpty());
        Assert.assertTrue(lastSecondBlock.getBlockHeader().getRawData().getNumber() + 1 == currentBlockNum);
        Assert.assertFalse(lastSecondBlock.getBlockHeader().getRawData().getParentHash().isEmpty());
        Assert.assertTrue(lastSecondBlock.getBlockHeader().getRawData().getWitnessId() >= 0);
        //logger.info("Last second test succesfully");
    }

    @Test(enabled = true)
    public void TestGetBlockById(){

        Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
        ByteString currentHash = currentBlock.getBlockHeader().getRawData().getParentHash();
        GrpcAPI.BytesMessage request = GrpcAPI.BytesMessage.newBuilder().setValue(currentHash).build();
        Block setIdOfBlock = blockingStubFull.getBlockById(request);
        Assert.assertTrue(setIdOfBlock.hasBlockHeader());
        Assert.assertFalse(setIdOfBlock.getBlockHeader().getWitnessSignature().isEmpty());
        Assert.assertTrue(setIdOfBlock.getBlockHeader().getRawData().getTimestamp() > 0);
        Assert.assertFalse(setIdOfBlock.getBlockHeader().getRawData().getWitnessAddress().isEmpty());
        logger.info(Long.toString(setIdOfBlock.getBlockHeader().getRawData().getNumber()));
        logger.info(Long.toString(currentBlock.getBlockHeader().getRawData().getNumber()));
        Assert.assertTrue(setIdOfBlock.getBlockHeader().getRawData().getNumber() + 1  == currentBlock.getBlockHeader().getRawData().getNumber());
        Assert.assertFalse(setIdOfBlock.getBlockHeader().getRawData().getParentHash().isEmpty());
        Assert.assertTrue(setIdOfBlock.getBlockHeader().getRawData().getWitnessId() >= 0);
        //logger.info("By ID test succesfully");






    }

    @AfterClass
    public void shutdown() throws InterruptedException {
        if (channelFull != null) {
            channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public Account queryAccount(String priKey,WalletGrpc.WalletBlockingStub blockingStubFull) {
        byte[] address;
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey= temKey;
        if (ecKey == null) {
            String pubKey = loadPubKey(); //04 PubKey[128]
            if (StringUtils.isEmpty(pubKey)) {
                logger.warn("Warning: QueryAccount failed, no wallet address !!");
                return null;
            }
            byte[] pubKeyAsc = pubKey.getBytes();
            byte[] pubKeyHex = Hex.decode(pubKeyAsc);
            ecKey = ECKey.fromPublicOnly(pubKeyHex);
        }
        return grpcQueryAccount(ecKey.getAddress(), blockingStubFull);
    }



    public static String loadPubKey() {
        char[] buf = new char[0x100];
        return String.valueOf(buf, 32, 130);
    }

    public byte[] getAddress(ECKey ecKey) {
        return ecKey.getAddress();
    }

    public Account grpcQueryAccount(byte[] address, WalletGrpc.WalletBlockingStub blockingStubFull) {
        ByteString addressBS = ByteString.copyFrom(address);
        Account request = Account.newBuilder().setAddress(addressBS).build();
        return blockingStubFull.getAccount(request);
        }

    public Block getBlock(long blockNum, WalletGrpc.WalletBlockingStub blockingStubFull) {
        NumberMessage.Builder builder = NumberMessage.newBuilder();
        builder.setNum(blockNum);
        return blockingStubFull.getBlockByNum(builder.build());

    }
}



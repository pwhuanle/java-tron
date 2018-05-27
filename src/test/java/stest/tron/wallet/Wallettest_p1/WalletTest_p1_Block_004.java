package stest.tron.wallet.Wallettest_p1;

import com.google.protobuf.ByteString;
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
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.utils.Base58;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

//import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

//import stest.tron.wallet.common.client.AccountComparator;

@Slf4j
public class WalletTest_p1_Block_004 {

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
    private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list").get(0);

    @BeforeClass
    public void beforeClass(){
        channelFull = ManagedChannelBuilder.forTarget(fullnode)
                .usePlaintext(true)
                .build();
        blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    }


    @Test(enabled = true)
    public void TestGetBlockByLimitNext(){
        //
        Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
        Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
        Assert.assertFalse(currentBlockNum < 0);
        while (currentBlockNum <= 5){
            logger.info("Now has very little block, Please wait");
            currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
            currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
        }

        GrpcAPI.BlockLimit.Builder builder = GrpcAPI.BlockLimit.newBuilder();
        builder.setStartNum(2);
        builder.setEndNum(4);
        GrpcAPI.BlockList blockList = blockingStubFull.getBlockByLimitNext(builder.build());
        Optional<GrpcAPI.BlockList> getBlockByLimitNext = Optional.ofNullable(blockList);
        Assert.assertTrue(getBlockByLimitNext.isPresent());
        Assert.assertTrue(getBlockByLimitNext.get().getBlockCount() == 2);
        logger.info(Long.toString(getBlockByLimitNext.get().getBlock(0).getBlockHeader().getRawData().getNumber()));
        logger.info(Long.toString(getBlockByLimitNext.get().getBlock(1).getBlockHeader().getRawData().getNumber()));
        Assert.assertTrue(getBlockByLimitNext.get().getBlock(0).getBlockHeader().getRawData().getNumber() < 4);
        Assert.assertTrue(getBlockByLimitNext.get().getBlock(1).getBlockHeader().getRawData().getNumber() < 4);
        Assert.assertTrue(getBlockByLimitNext.get().getBlock(0).hasBlockHeader());
        Assert.assertTrue(getBlockByLimitNext.get().getBlock(1).hasBlockHeader());
        Assert.assertFalse(getBlockByLimitNext.get().getBlock(0).getBlockHeader().getRawData().getParentHash().isEmpty());
        Assert.assertFalse(getBlockByLimitNext.get().getBlock(1).getBlockHeader().getRawData().getParentHash().isEmpty());
    }

    @Test(enabled = true)
    public void TestGetBlockByExceptionLimitNext(){
        Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
        Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
        Assert.assertFalse(currentBlockNum < 0);
        while (currentBlockNum <= 5){
            logger.info("Now has very little block, Please wait");
            currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
            currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
        }

        //From -1 to 1
        GrpcAPI.BlockLimit.Builder builder = GrpcAPI.BlockLimit.newBuilder();
        builder.setStartNum(-1);
        builder.setEndNum(1);
        GrpcAPI.BlockList blockList = blockingStubFull.getBlockByLimitNext(builder.build());
        Optional<GrpcAPI.BlockList> getBlockByLimitNext = Optional.ofNullable(blockList);
        Assert.assertTrue(getBlockByLimitNext.get().getBlockCount() == 0);

        //From 3 to 3
        builder = GrpcAPI.BlockLimit.newBuilder();
        builder.setStartNum(3);
        builder.setEndNum(3);
        blockList = blockingStubFull.getBlockByLimitNext(builder.build());
        getBlockByLimitNext = Optional.ofNullable(blockList);
        Assert.assertTrue(getBlockByLimitNext.get().getBlockCount() == 0);


        //From 4 to 2
        builder = GrpcAPI.BlockLimit.newBuilder();
        builder.setStartNum(4);
        builder.setEndNum(2);
        blockList = blockingStubFull.getBlockByLimitNext(builder.build());
        getBlockByLimitNext = Optional.ofNullable(blockList);
        Assert.assertTrue(getBlockByLimitNext.get().getBlockCount() == 0);

        //From 999999990 to 999999999
        builder = GrpcAPI.BlockLimit.newBuilder();
        builder.setStartNum(999999990);
        builder.setEndNum(999999999);
        blockList = blockingStubFull.getBlockByLimitNext(builder.build());
        getBlockByLimitNext = Optional.ofNullable(blockList);
        Assert.assertTrue(getBlockByLimitNext.get().getBlockCount() == 0);
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



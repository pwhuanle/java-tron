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
import org.tron.api.GrpcAPI.Return;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Contract.FreezeBalanceContract;
import org.tron.protos.Contract.UnfreezeBalanceContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.TransactionUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WalletTest_p1_Witness_001 {

    //testng001、testng002、testng003、testng004
    private final static  String testKey001     = "8CB4480194192F30907E14B52498F594BD046E21D7C4D8FE866563A6760AC891";
    private final static  String testKey002     = "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
    private final static  String testKey003     = "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";
    private final static  String testKey004     = "592BB6C9BB255409A6A43EFD18E6A74FECDDCCE93A40D96B70FBE334E6361E32";
    private final static  String no_frozen_balance_testKey = "8CB4480194192F30907E14B52498F594BD046E21D7C4D8FE866563A6760AC891";

    //testng001、testng002、testng003、testng004
    private static final byte[] BACK_ADDRESS    = Base58.decodeFromBase58Check("27YcHNYcxHGRf5aujYzWQaJSpQ4WN4fJkiU");
    private static final byte[] FROM_ADDRESS    = Base58.decodeFromBase58Check("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv");
    private static final byte[] TO_ADDRESS      = Base58.decodeFromBase58Check("27iDPGt91DX3ybXtExHaYvrgDt5q5d6EtFM");
    private static final byte[] NEED_CR_ADDRESS = Base58.decodeFromBase58Check("27QEkeaPHhUSQkw9XbxX3kCKg684eC2w67T");
    private static final byte[] NO_FROZEN_ADDRESS = Base58.decodeFromBase58Check("27YcHNYcxHGRf5aujYzWQaJSpQ4WN4fJkiU");

    private ManagedChannel channelFull = null;
    private ManagedChannel search_channelFull = null;
    private WalletGrpc.WalletBlockingStub blockingStubFull = null;
    private WalletGrpc.WalletBlockingStub search_blockingStubFull = null;
    private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list").get(0);
    private String search_fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list").get(1);


    @BeforeClass
    public void beforeClass(){
        channelFull = ManagedChannelBuilder.forTarget(fullnode)
                .usePlaintext(true)
                .build();
        blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

        search_channelFull = ManagedChannelBuilder.forTarget(search_fullnode)
                .usePlaintext(true)
                .build();
        search_blockingStubFull = WalletGrpc.newBlockingStub(search_channelFull);
    }

    @Test
    public void TestVoteWitness(){
        HashMap<String,String> small_vote_map=new HashMap<String,String>();
        small_vote_map.put("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv", "1");
        HashMap<String,String> wrong_vote_map=new HashMap<String,String>();
        wrong_vote_map.put("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv", "-1");
        HashMap<String,String> zero_vote_map=new HashMap<String,String>();
        zero_vote_map.put("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv", "0");

        HashMap<String,String>  very_large_map = new HashMap<String,String>();
        very_large_map.put("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv","1000000000");
        HashMap<String,String>  wrong_drop_map = new HashMap<String,String>();
        wrong_drop_map.put("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv","10000000000000000");


        //Vote failed due to no freeze balance.
        //Assert.assertFalse(VoteWitness(small_vote_map, NO_FROZEN_ADDRESS, no_frozen_balance_testKey));

        //Freeze balance to get vote ability.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(FreezeBalance(FROM_ADDRESS,10000000L, 3L, testKey002));

        //Vote failed when the vote is large than the freeze balance.
        Assert.assertFalse(VoteWitness(very_large_map, FROM_ADDRESS, testKey002));
        //Vote failed due to 0 vote.
        Assert.assertFalse(VoteWitness(zero_vote_map,FROM_ADDRESS,testKey002));
        //Vote failed duo to -1 vote.
        Assert.assertFalse(VoteWitness(wrong_vote_map,FROM_ADDRESS,testKey002));
        //Vote is so large, vote failed.
        Assert.assertFalse(VoteWitness(wrong_drop_map,FROM_ADDRESS,testKey002));

        //Vote success, the second latest vote is cover by the latest vote.
        Assert.assertTrue(VoteWitness(small_vote_map, FROM_ADDRESS, testKey002));
        Assert.assertTrue(VoteWitness(small_vote_map, FROM_ADDRESS, testKey002));
        Assert.assertTrue(VoteWitness(small_vote_map, FROM_ADDRESS, testKey002));
    }

    @AfterClass
    public void shutdown() throws InterruptedException {
        if (channelFull != null) {
            channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (search_channelFull != null) {
            search_channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public Boolean VoteWitness(HashMap<String, String> witness, byte[] Address, String priKey){

        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey= temKey;
        Account beforeVote = queryAccount(ecKey, blockingStubFull);
        Long beforeVoteNum = 0L;
        if (beforeVote.getVotesCount() != 0 ){
            beforeVoteNum = beforeVote.getVotes(0).getVoteCount();
        }

        Contract.VoteWitnessContract.Builder builder = Contract.VoteWitnessContract.newBuilder();
        builder.setOwnerAddress(ByteString.copyFrom(Address));
        for (String addressBase58 : witness.keySet()) {
            String value = witness.get(addressBase58);
            long count = Long.parseLong(value);
            Contract.VoteWitnessContract.Vote.Builder voteBuilder = Contract.VoteWitnessContract.Vote
                    .newBuilder();
            byte[] address = WalletClient.decodeFromBase58Check(addressBase58);
            if (address == null) {
                continue;
            }
            voteBuilder.setVoteAddress(ByteString.copyFrom(address));
            voteBuilder.setVoteCount(count);
            builder.addVotes(voteBuilder.build());
        }

        Contract.VoteWitnessContract contract = builder.build();

        Transaction transaction = blockingStubFull.voteWitnessAccount(contract);
        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            logger.info(Integer.toString(transaction.getRawData().getAuthsCount()));
            logger.info("transaction == null");
            return false;
        }
        transaction = signTransaction(ecKey,transaction);
        GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);

        if (response.getResult() == false){
            logger.info("response.getresult() == false");
            return false;
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Account afterVote = queryAccount(ecKey, search_blockingStubFull);
        //Long afterVoteNum = afterVote.getVotes(0).getVoteCount();
        for (String key : witness.keySet()) {
            for (int j = 0; j < afterVote.getVotesCount(); j++) {
                logger.info(Long.toString(Long.parseLong(witness.get(key))));
                logger.info(key);
                if (key.equals("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv")){
                    logger.info("catch it");
                    logger.info(Long.toString(afterVote.getVotes(j).getVoteCount()));
                    logger.info(Long.toString(Long.parseLong(witness.get(key))));
                    Assert.assertTrue(afterVote.getVotes(j).getVoteCount() == Long.parseLong(witness.get(key)));
                }

            }
        }
         return true;
        }

    public Boolean FreezeBalance(byte[] Address, long freezeBalance, long freezeDuration, String priKey){
        byte[] address = Address;
        long frozen_balance = freezeBalance;
        long frozen_duration = freezeDuration;

        //String priKey = testKey002;
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey= temKey;
        Account beforeFronzen = queryAccount(ecKey, blockingStubFull);
        Long beforeFrozenBalance = 0L;
        //Long beforeBandwidth     = beforeFronzen.getBandwidth();
        if(beforeFronzen.getFrozenCount()!= 0){
            beforeFrozenBalance = beforeFronzen.getFrozen(0).getFrozenBalance();
            //beforeBandwidth     = beforeFronzen.getBandwidth();
            //logger.info(Long.toString(beforeFronzen.getBandwidth()));
            logger.info(Long.toString(beforeFronzen.getFrozen(0).getFrozenBalance()));
        }

        FreezeBalanceContract.Builder builder = FreezeBalanceContract.newBuilder();
        ByteString byteAddreess = ByteString.copyFrom(address);

        builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozen_balance)
                .setFrozenDuration(frozen_duration);


        FreezeBalanceContract contract = builder.build();
        Transaction transaction = blockingStubFull.freezeBalance(contract);

        if (transaction == null || transaction.getRawData().getContractCount() == 0){
            return false;
        }

        transaction = TransactionUtils.setTimestamp(transaction);
        transaction = TransactionUtils.sign(transaction, ecKey);
        Return response = blockingStubFull.broadcastTransaction(transaction);

        if (response.getResult() == false){
            return false;
        }

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Account afterFronzen = queryAccount(ecKey, search_blockingStubFull);
        Long afterFrozenBalance = afterFronzen.getFrozen(0).getFrozenBalance();
        //Long afterBandwidth     = afterFronzen.getBandwidth();
        //logger.info(Long.toString(afterFronzen.getBandwidth()));
        //logger.info(Long.toString(afterFronzen.getFrozen(0).getFrozenBalance()));
        //logger.info(Integer.toString(search.getFrozenCount()));
        logger.info("afterfrozenbalance =" + Long.toString(afterFrozenBalance) + "beforefrozenbalance =  " + beforeFrozenBalance +
        "freezebalance = " + Long.toString(freezeBalance));
        //logger.info("afterbandwidth = " + Long.toString(afterBandwidth) + " beforebandwidth = " + Long.toString(beforeBandwidth));
        //if ((afterFrozenBalance - beforeFrozenBalance != freezeBalance) ||
         //       (freezeBalance * frozen_duration -(afterBandwidth - beforeBandwidth) !=0)){
          //  logger.info("After 20 second, two node still not synchronous");
       // }
        Assert.assertTrue(afterFrozenBalance - beforeFrozenBalance == freezeBalance);
        //Assert.assertTrue(freezeBalance * frozen_duration - (afterBandwidth - beforeBandwidth) <= 1000000);
        return true;


    }

    public boolean UnFreezeBalance(byte[] Address, String priKey) {
        byte[] address = Address;

        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey= temKey;
        Account search = queryAccount(ecKey, blockingStubFull);

        UnfreezeBalanceContract.Builder builder = UnfreezeBalanceContract
                .newBuilder();
        ByteString byteAddreess = ByteString.copyFrom(address);

        builder.setOwnerAddress(byteAddreess);

        UnfreezeBalanceContract contract = builder.build();


        Transaction transaction = blockingStubFull.unfreezeBalance(contract);

        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            return false;
        }

        transaction = TransactionUtils.setTimestamp(transaction);
        transaction = TransactionUtils.sign(transaction, ecKey);
        Return response = blockingStubFull.broadcastTransaction(transaction);
        if (response.getResult() == false){
            return false;
        }
        else{
            return true;
        }
    }


    public Account queryAccount(ECKey ecKey,WalletGrpc.WalletBlockingStub blockingStubFull) {
        byte[] address;
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

    private Transaction signTransaction(ECKey ecKey, Transaction transaction) {
        if (ecKey == null || ecKey.getPrivKey() == null) {
            logger.warn("Warning: Can't sign,there is no private key !!");
            return null;
        }
        transaction = TransactionUtils.setTimestamp(transaction);
        return TransactionUtils.sign(transaction, ecKey);
    }
}



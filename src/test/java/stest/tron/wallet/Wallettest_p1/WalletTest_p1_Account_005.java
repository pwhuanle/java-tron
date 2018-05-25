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
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
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
public class WalletTest_p1_Account_005 {

    //testng001、testng002、testng003、testng004
    private final static  String testKey001     = "8CB4480194192F30907E14B52498F594BD046E21D7C4D8FE866563A6760AC891";
    private final static  String testKey002     = "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
    private final static  String testKey003     = "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";
    private final static  String testKey004     = "592BB6C9BB255409A6A43EFD18E6A74FECDDCCE93A40D96B70FBE334E6361E32";
    private final static  String not_witness_testKey = "8CB4480194192F30907E14B52498F594BD046E21D7C4D8FE866563A6760AC891";


    //testng001、testng002、testng003、testng004
    private static final byte[] BACK_ADDRESS    = Base58.decodeFromBase58Check("27YcHNYcxHGRf5aujYzWQaJSpQ4WN4fJkiU");
    private static final byte[] FROM_ADDRESS    = Base58.decodeFromBase58Check("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv");
    private static final byte[] TO_ADDRESS      = Base58.decodeFromBase58Check("27iDPGt91DX3ybXtExHaYvrgDt5q5d6EtFM");
    private static final byte[] NEED_CR_ADDRESS = Base58.decodeFromBase58Check("27QEkeaPHhUSQkw9XbxX3kCKg684eC2w67T");
    private static final byte[] NOT_WITNESS = Base58.decodeFromBase58Check("27QEkeaPHhUSQkw9XbxX3kCKg684eC2w67T");

    private ManagedChannel channelFull = null;
    private ManagedChannel search_channelFull = null;
    private WalletGrpc.WalletBlockingStub blockingStubFull = null;
    private WalletGrpc.WalletBlockingStub search_blockingStubFull = null;
    //private String fullnode = "39.105.111.178:50051";
    //private String search_fullnode = "39.105.104.137:50051";
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
    public void TestWithdrawBalance(){
        //如果不是witness账户，提示错误
        Assert.assertFalse(WithdrawBalance(NOT_WITNESS, not_witness_testKey));
        //如果上次赎回时间在1天以前，witness可以赎回资产，balance增加赎回的资产。由于再次赎回要等待1天，所以该用例暂时不判断结果
        //Assert.assertTrue(WithdrawBalance(FROM_ADDRESS,testKey002));
        WithdrawBalance(FROM_ADDRESS,testKey002);
        //如果上次赎回时间在1天之内，不可赎回
        Assert.assertFalse(WithdrawBalance(FROM_ADDRESS,testKey002));


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

    public boolean WithdrawBalance(byte[] address, String priKey){
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey= temKey;

        Contract.WithdrawBalanceContract.Builder builder = Contract.WithdrawBalanceContract
                .newBuilder();
        ByteString byteAddreess = ByteString.copyFrom(address);
        builder.setOwnerAddress(byteAddreess);
        Contract.WithdrawBalanceContract contract =builder.build();

        Transaction transaction = blockingStubFull.withdrawBalance(contract);
        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            return false;
        }

        transaction = signTransaction(ecKey,transaction);
        Return response = blockingStubFull.broadcastTransaction(transaction);
        if (response.getResult() == false){
            return false;
        }
        logger.info("test withdraw" + priKey);
        return true;

    }




/*    public boolean withdrawBalance() {
        Contract.WithdrawBalanceContract contract = createWithdrawBalanceContract();

        Transaction transaction = rpcCli.createTransaction(contract);

        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            return false;
        }

        transaction = signTransaction(transaction);
        return rpcCli.broadcastTransaction(transaction);
    }*/


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
            return false;
        }
        transaction = signTransaction(ecKey,transaction);
        Return response = blockingStubFull.broadcastTransaction(transaction);

        if (response.getResult() == false){
            return false;
        }
        Account afterVote = queryAccount(ecKey, search_blockingStubFull);
        //Long afterVoteNum = afterVote.getVotes(0).getVoteCount();
        for (String key : witness.keySet()) {
            for (int j = 0; j < afterVote.getVotesCount(); j++) {
                if (key.equals(afterVote.getVotes(j).getVoteAddress())) {
                    Long afterVoteNum = Long.parseLong(witness.get(key));
                    Assert.assertTrue(afterVoteNum == afterVote.getVotes(j).getVoteCount());
                    logger.info("test equal vote");
                }
            }
        }
        return true;
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



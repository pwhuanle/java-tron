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
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.TransactionUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import stest.tron.wallet.common.client.AccountComparator;

@Slf4j
public class WalletTest_p1_Account_003 {

    //testng001、testng002、testng003、testng004
    private final static String testKey001 = "8CB4480194192F30907E14B52498F594BD046E21D7C4D8FE866563A6760AC891";
    private final static String testKey002 = "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
    private final static String testKey003 = "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";
    private final static String testKey004 = "592BB6C9BB255409A6A43EFD18E6A74FECDDCCE93A40D96B70FBE334E6361E32";
    private final static String lowBalTest = "86ff0c39337e9e97526c80af51f0e80411f5a1251473035f380f3671c1aa2b4b";

    //testng001、testng002、testng003、testng004
    private static final byte[] BACK_ADDRESS = Base58.decodeFromBase58Check("27YcHNYcxHGRf5aujYzWQaJSpQ4WN4fJkiU");
    private static final byte[] FROM_ADDRESS = Base58.decodeFromBase58Check("27WvzgdLiUvNAStq2BCvA1LZisdD3fBX8jv");
    private static final byte[] TO_ADDRESS = Base58.decodeFromBase58Check("27iDPGt91DX3ybXtExHaYvrgDt5q5d6EtFM");
    private static final byte[] NEED_CR_ADDRESS = Base58.decodeFromBase58Check("27QEkeaPHhUSQkw9XbxX3kCKg684eC2w67T");
    private static final byte[] Low_Bal_ADDRESS = Base58.decodeFromBase58Check("27XeWZUtufGk8jdjF3m1tuPnnRqqKgzS3pT");
    private static final byte[] INVAILD_ADDRESS = Base58.decodeFromBase58Check("27cu1ozb4mX3m2afY68FSAqn3HmMp815d48");

    private static final long now = System.currentTimeMillis();
    private static final String name = "testAssetIssue_" + Long.toString(now);
    private static final long TotalSupply = now;
    String Description = "just-test";
    String Url = "https://github.com/tronprotocol/wallet-cli/";

    private ManagedChannel channelFull = null;
    private WalletGrpc.WalletBlockingStub blockingStubFull = null;
    private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list").get(0);

    @BeforeClass
    public void beforeClass() {
        channelFull = ManagedChannelBuilder.forTarget(fullnode)
                .usePlaintext(true)
                .build();
        blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    }

    @Test
    public void TestCreateAccount() {
        Account noCreateAccount = queryAccount(lowBalTest, blockingStubFull);
        if (noCreateAccount.getAccountName().isEmpty()) {
            Assert.assertTrue(Sendcoin(Low_Bal_ADDRESS, 1L, FROM_ADDRESS, testKey002));
            //Assert.assertTrue(Sendcoin(Low_Bal_ADDRESS, 1000000L, FROM_ADDRESS, testKey002));
            noCreateAccount = queryAccount(lowBalTest, blockingStubFull);
            logger.info(Long.toString(noCreateAccount.getBalance()));
            Assert.assertTrue(noCreateAccount.getBalance() == 1);

            //TestVoteToNonWitnessAccount
            HashMap<String,String> vote_to_non_witness_account=new HashMap<String,String>();
            vote_to_non_witness_account.put("27XeWZUtufGk8jdjF3m1tuPnnRqqKgzS3pT", "1");
            HashMap<String,String> vote_to_invaild_address=new HashMap<String,String>();
            vote_to_invaild_address.put("27cu1ozb4mX3m2afY68FSAqn3HmMp815d48", "1");
            Assert.assertTrue(FreezeBalance(FROM_ADDRESS,10000000L, 3L,testKey002));
            Assert.assertFalse(VoteWitness(vote_to_non_witness_account,FROM_ADDRESS,testKey002));
            Assert.assertFalse(VoteWitness(vote_to_invaild_address,FROM_ADDRESS,testKey002));

            logger.info("vote to non witness account ok!!!");

        } else {
            logger.info("Please confirm wither the create account test is pass, or you will do it by manual");
        }
    }

    @Test
    public void TestUpdateAccount() {
        Account tryToUpdateAccount = queryAccount(lowBalTest, blockingStubFull);
        if (tryToUpdateAccount.getAccountName().isEmpty()) {
            Assert.assertFalse(updateAccount(Low_Bal_ADDRESS,"1short1".getBytes(),lowBalTest));
            Assert.assertFalse(updateAccount(Low_Bal_ADDRESS,"verylongnamehas33char111111111111".getBytes(),lowBalTest));
            Assert.assertFalse(updateAccount(Low_Bal_ADDRESS,"test Name".getBytes(),lowBalTest));
            Assert.assertFalse(updateAccount(Low_Bal_ADDRESS,"中文非法名字".getBytes(),lowBalTest));
            Assert.assertFalse(updateAccount(Low_Bal_ADDRESS,"".getBytes(),lowBalTest));
/*            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            Assert.assertTrue(updateAccount(Low_Bal_ADDRESS, "testName".getBytes(), lowBalTest));
            tryToUpdateAccount = queryAccount(lowBalTest, blockingStubFull);
            Assert.assertFalse(tryToUpdateAccount.getAccountName().isEmpty());
            Assert.assertFalse(updateAccount(Low_Bal_ADDRESS,"secondUpdateName".getBytes(), lowBalTest));
        } else {
            logger.info("This account had already has a name, please confirm wither you should do the updatea ccount test by manual");
        }
    }

    @Test(enabled = true)
    public void TestNoBalanceCreateAssetIssue() {
        Account lowaccount = queryAccount(lowBalTest,blockingStubFull);
        if (lowaccount.getBalance() > 0){
/*            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            Assert.assertTrue(Sendcoin(TO_ADDRESS,lowaccount.getBalance(),Low_Bal_ADDRESS,lowBalTest));
        }
        //Create AssetIssue failed when there is no enough balance.
        Assert.assertFalse(CreateAssetIssue(Low_Bal_ADDRESS, name, TotalSupply, 1, 1, now+ 100000000L, now + 10000000000L, 2, Description, Url, lowBalTest));
        logger.info("nobalancecreateassetissue");
    }

    @Test
    public void TestNoBalanceTransferTrx() {
        //Send Coin failed when there is no enough balance.
        Assert.assertFalse(Sendcoin(TO_ADDRESS, 100000000000000000L, Low_Bal_ADDRESS, lowBalTest));
    }

    @Test
    public void TestNoBalanceCreateWitness() {
        //Apply to be super witness failed when no enough balance.
        Assert.assertFalse(CreateWitness(Low_Bal_ADDRESS, FROM_ADDRESS, lowBalTest));
    }

    @Test
    public void TestNoFreezeBalanceToUnfreezeBalance(){
        //Unfreeze account failed when no freeze balance
        Account noFreezeAccount = queryAccount(lowBalTest,blockingStubFull);
        if (noFreezeAccount.getFrozenCount() == 0){
            Assert.assertFalse(UnFreezeBalance(Low_Bal_ADDRESS,lowBalTest));
        }
        else{
            logger.info("This account has freeze balance, please test this case for manual");
        }
    }


/*    @Test
    public void TestVoteToNonWitnessAccount(){
        HashMap<String,String> vote_to_non_witness_account=new HashMap<String,String>();
        vote_to_non_witness_account.put("27XeWZUtufGk8jdjF3m1tuPnnRqqKgzS3pT", "1");
        HashMap<String,String> vote_to_invaild_address=new HashMap<String,String>();
        vote_to_invaild_address.put("27cu1ozb4mX3m2afY68FSAqn3HmMp815d48", "1");
        Assert.assertTrue(FreezeBalance(FROM_ADDRESS,10000000L, 3L,testKey002));
        Assert.assertFalse(VoteWitness(vote_to_non_witness_account,FROM_ADDRESS,testKey002));
        Assert.assertFalse(VoteWitness(vote_to_invaild_address,FROM_ADDRESS,testKey002));

        logger.info("vote to non witness account ok!!!");

    }*/

    @AfterClass
    public void shutdown() throws InterruptedException {
        if (channelFull != null) {
            channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public Boolean CreateWitness(byte[] owner, byte[] url, String priKey) {
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey = temKey;

        Contract.WitnessCreateContract.Builder builder = Contract.WitnessCreateContract.newBuilder();
        builder.setOwnerAddress(ByteString.copyFrom(owner));
        builder.setUrl(ByteString.copyFrom(url));
        Contract.WitnessCreateContract contract = builder.build();
        Protocol.Transaction transaction = blockingStubFull.createWitness(contract);
        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            return false;
        }
        transaction = signTransaction(ecKey, transaction);
        GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
        if (response.getResult() == false) {
            return false;
        } else {
            return true;
        }
    }


    public Boolean Sendcoin(byte[] to, long amount, byte[] owner, String priKey) {
        //String priKey = testKey002;
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey = temKey;

        Contract.TransferContract.Builder builder = Contract.TransferContract.newBuilder();
        ByteString bsTo = ByteString.copyFrom(to);
        ByteString bsOwner = ByteString.copyFrom(owner);
        builder.setToAddress(bsTo);
        builder.setOwnerAddress(bsOwner);
        builder.setAmount(amount);

        Contract.TransferContract contract = builder.build();
        Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);
        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            logger.info("transaction == null");
            return false;
        }
        transaction = signTransaction(ecKey, transaction);
        GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
        if (response.getResult() == false) {
            logger.info(ByteArray.toStr(response.getMessage().toByteArray()));
            return false;
        } else {
            return true;
        }
    }

    public Boolean CreateAssetIssue(byte[] address, String name, Long TotalSupply, Integer TrxNum, Integer IcoNum, Long StartTime, Long EndTime,
                                    Integer VoteScore, String Description, String URL, String priKey) {
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey = temKey;

        try {
            Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
            builder.setOwnerAddress(ByteString.copyFrom(address));
            builder.setName(ByteString.copyFrom(name.getBytes()));
            builder.setTotalSupply(TotalSupply);
            builder.setTrxNum(TrxNum);
            builder.setNum(IcoNum);
            builder.setStartTime(StartTime);
            builder.setEndTime(EndTime);
            builder.setVoteScore(VoteScore);
            builder.setDescription(ByteString.copyFrom(Description.getBytes()));
            builder.setUrl(ByteString.copyFrom(URL.getBytes()));

            Protocol.Transaction transaction = blockingStubFull.createAssetIssue(builder.build());
            if (transaction == null || transaction.getRawData().getContractCount() == 0) {
                logger.info("Please check!!! transaction == null");
                return false;
            }
            transaction = signTransaction(ecKey, transaction);
            GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
            if (response.getResult() == false) {
                logger.info("Please check!!! response.getresult==false");
                return false;
            } else {
                logger.info(name);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    class AccountComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return Long.compare(((Account) o2).getBalance(), ((Account) o1).getBalance());
        }
    }

    public Account queryAccount(String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
        byte[] address;
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey = temKey;
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

    private Protocol.Transaction signTransaction(ECKey ecKey, Protocol.Transaction transaction) {
        if (ecKey == null || ecKey.getPrivKey() == null) {
            logger.warn("Warning: Can't sign,there is no private key !!");
            return null;
        }
        transaction = TransactionUtils.setTimestamp(transaction);
        return TransactionUtils.sign(transaction, ecKey);
    }


    public boolean updateAccount(byte[] addressBytes, byte[] accountNameBytes, String priKey) {
        ECKey temKey = null;
        try {
            BigInteger priK = new BigInteger(priKey, 16);
            temKey = ECKey.fromPrivate(priK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ECKey ecKey = temKey;


        Contract.AccountUpdateContract.Builder builder = Contract.AccountUpdateContract.newBuilder();
        ByteString basAddreess = ByteString.copyFrom(addressBytes);
        ByteString bsAccountName = ByteString.copyFrom(accountNameBytes);

        builder.setAccountName(bsAccountName);
        builder.setOwnerAddress(basAddreess);

        Contract.AccountUpdateContract contract = builder.build();
        Protocol.Transaction transaction = blockingStubFull.updateAccount(contract);

        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            logger.info("Please check!!! transaction == null");
            return false;
        }
        transaction = signTransaction(ecKey, transaction);
        GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
        if (response.getResult() == false) {
            logger.info("Please check!!! response.getresult==false");
            logger.info(ByteArray.toStr(response.getMessage().toByteArray()));
            return false;
        } else {
            logger.info(name);
            return true;
        }
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
        Contract.UnfreezeBalanceContract.Builder builder = Contract.UnfreezeBalanceContract
                .newBuilder();
        ByteString byteAddreess = ByteString.copyFrom(address);

        builder.setOwnerAddress(byteAddreess);

        Contract.UnfreezeBalanceContract contract = builder.build();


        Protocol.Transaction transaction = blockingStubFull.unfreezeBalance(contract);

        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            return false;
        }

        transaction = TransactionUtils.setTimestamp(transaction);
        transaction = TransactionUtils.sign(transaction, ecKey);
        GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
        if (response.getResult() == false){
            return false;
        }
        else{
            return true;
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

        Protocol.Transaction transaction = blockingStubFull.voteWitnessAccount(contract);
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


        Contract.FreezeBalanceContract.Builder builder = Contract.FreezeBalanceContract.newBuilder();
        ByteString byteAddreess = ByteString.copyFrom(address);

        builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozen_balance)
                .setFrozenDuration(frozen_duration);


        Contract.FreezeBalanceContract contract = builder.build();
        Protocol.Transaction transaction = blockingStubFull.freezeBalance(contract);

        if (transaction == null || transaction.getRawData().getContractCount() == 0){
            return false;
        }

        transaction = TransactionUtils.setTimestamp(transaction);
        transaction = TransactionUtils.sign(transaction, ecKey);
        GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);

        if (response.getResult() == false){
            return false;
        }
        return true;


    }

    }




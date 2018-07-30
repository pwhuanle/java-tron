package org.tron.core.net.node;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.application.Application;
import org.tron.common.application.ApplicationFactory;
import org.tron.common.crypto.ECKey;
import org.tron.common.overlay.client.PeerClient;
import org.tron.common.overlay.discover.node.Node;
import org.tron.common.overlay.server.Channel;
import org.tron.common.overlay.server.ChannelManager;
import org.tron.common.overlay.server.SyncPool;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ReflectUtils;
import org.tron.core.Constant;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.ByteArrayWrapper;
import org.tron.core.db.Manager;
import org.tron.core.net.message.BlockMessage;
import org.tron.core.net.peer.PeerConnection;
import org.tron.core.services.RpcApiService;
import org.tron.core.services.WitnessService;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.BlockHeader;
import org.tron.protos.Protocol.Inventory.InventoryType;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;


@Slf4j
public class HandleBlockMessageTest {

    private static AnnotationConfigApplicationContext context;
    private NodeImpl node;
    RpcApiService rpcApiService;
    PeerClient peerClient;
    ChannelManager channelManager;
    SyncPool pool;
    Application appT;
    Manager dbManager;

    private static final String dbPath = "output-HandleBlockMessageTest";
    private static final String dbDirectory = "db_HandleBlockMessage_test";
    private static final String indexDirectory = "index_HandleBlockMessage_test";

    private static Boolean deleteFolder(File index) {
        if (!index.isDirectory() || index.listFiles().length <= 0) {
            return index.delete();
        }
        for (File file : index.listFiles()) {
            if (null != file && !deleteFolder(file)) {
                return false;
            }
        }
        return index.delete();
    }

    @Test
    public void testHandleBlockMessage() throws Exception {
        PeerConnection peer = new PeerConnection();

        //收到同步请求块
        BlockCapsule headBlockCapsule = dbManager.getHead();
        BlockCapsule syncblockCapsule = generateOneBlockCapsule(headBlockCapsule);
        BlockMessage blockMessage = new BlockMessage(syncblockCapsule);
        peer.getSyncBlockRequested().put(blockMessage.getBlockId(), System.currentTimeMillis());
        node.onMessage(peer, blockMessage);
        Assert.assertEquals(peer.getSyncBlockRequested().isEmpty(), true);

        //收到广播请求块
        BlockCapsule advblockCapsule = generateOneBlockCapsule(headBlockCapsule);
        BlockMessage advblockMessage = new BlockMessage(advblockCapsule);
        peer.getAdvObjWeRequested().put(new Item(advblockMessage.getBlockId(), InventoryType.BLOCK), System.currentTimeMillis());
        node.onMessage(peer, advblockMessage);
        Assert.assertEquals(peer.getAdvObjWeRequested().size(), 0);

        //收到非请求块
        BlockCapsule blockCapsule = generateOneBlockCapsule(headBlockCapsule);
        blockMessage = new BlockMessage(blockCapsule);
        BlockCapsule blockCapsuleOther = generateOneBlockCapsule(blockCapsule);
        BlockMessage blockMessageOther = new BlockMessage(blockCapsuleOther);

        peer.getSyncBlockRequested().put(blockMessage.getBlockId(), System.currentTimeMillis());
        node.onMessage(peer, blockMessageOther);
        Assert.assertEquals(peer.getSyncBlockRequested().isEmpty(), false);
    }

    //根据父块生成一个区块
    private BlockCapsule generateOneBlockCapsule(BlockCapsule parentCapsule) {
        ByteString witnessAddress = ByteString.copyFrom(
                ECKey.fromPrivate(
                        ByteArray.fromHexString(
                                Args.getInstance().getLocalWitnesses().getPrivateKey()))
                        .getAddress());
        BlockHeader.raw raw = BlockHeader.raw.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setParentHash(parentCapsule.getBlockId().getByteString())
                .setNumber(parentCapsule.getNum() + 1)
                .setWitnessAddress(witnessAddress)
                .setWitnessId(1).build();
        BlockHeader blockHeader = BlockHeader.newBuilder()
                .setRawData(raw)
                .build();

        Block block = Block.newBuilder().setBlockHeader(blockHeader).build();

        BlockCapsule blockCapsule = new BlockCapsule(block);
        blockCapsule.setMerkleRoot();
        blockCapsule.sign(
                ByteArray.fromHexString(Args.getInstance().getLocalWitnesses().getPrivateKey()));
        blockCapsule.setMerkleRoot();
        blockCapsule.sign(
                ByteArray.fromHexString(Args.getInstance().getLocalWitnesses().getPrivateKey()));

        return blockCapsule;
    }

    private static boolean go = false;

    @Before
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Full node running.");
                Args.setParam(
                        new String[]{
                                "--output-directory", dbPath,
                                "--storage-db-directory", dbDirectory,
                                "--storage-index-directory", indexDirectory
                        },
                        Constant.TEST_CONF
                );
                Args cfgArgs = Args.getInstance();
                cfgArgs.setNodeListenPort(17894);
                cfgArgs.setNodeDiscoveryEnable(false);
                cfgArgs.getSeedNode().getIpList().clear();
                cfgArgs.setNeedSyncCheck(false);
                cfgArgs.setNodeExternalIp("127.0.0.1");

                context = new AnnotationConfigApplicationContext(DefaultConfig.class);

                if (cfgArgs.isHelp()) {
                    logger.info("Here is the help message.");
                    return;
                }
                appT = ApplicationFactory.create(context);
                rpcApiService = context.getBean(RpcApiService.class);
                appT.addService(rpcApiService);
                if (cfgArgs.isWitness()) {
                    appT.addService(new WitnessService(appT, context));
                }
//        appT.initServices(cfgArgs);
//        appT.startServices();
//        appT.startup();
                node = context.getBean(NodeImpl.class);
                peerClient = context.getBean(PeerClient.class);
                channelManager = context.getBean(ChannelManager.class);
                pool = context.getBean(SyncPool.class);
                dbManager = context.getBean(Manager.class);
                NodeDelegate nodeDelegate = new NodeDelegateImpl(dbManager);
                node.setNodeDelegate(nodeDelegate);
                pool.init(node);
                prepare();
                rpcApiService.blockUntilShutdown();
            }
        }).start();
        int tryTimes = 0;
        while (tryTimes < 10 && (node == null || peerClient == null
                || channelManager == null || pool == null || !go)) {
            try {
                logger.info("node:{},peerClient:{},channelManager:{},pool:{},{}", node, peerClient,
                        channelManager, pool, go);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                ++tryTimes;
            }
        }
    }

    private void prepare() {
        try {
            ExecutorService advertiseLoopThread = ReflectUtils.getFieldValue(node, "broadPool");
            advertiseLoopThread.shutdownNow();

            ReflectUtils.setFieldValue(node, "isAdvertiseActive", false);
            ReflectUtils.setFieldValue(node, "isFetchActive", false);

            Node node = new Node(
                    "enode://e437a4836b77ad9d9ffe73ee782ef2614e6d8370fcf62191a6e488276e23717147073a7ce0b444d485fff5a0c34c4577251a7a990cf80d8542e21b95aa8c5e6c@127.0.0.1:17894");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    peerClient.connect(node.getHost(), node.getPort(), node.getHexId());
                }
            }).start();
            Thread.sleep(1000);
            Map<ByteArrayWrapper, Channel> activePeers = ReflectUtils
                    .getFieldValue(channelManager, "activePeers");
            int tryTimes = 0;
            while (MapUtils.isEmpty(activePeers) && ++tryTimes < 10) {
                Thread.sleep(1000);
            }
            go = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void removeDb() {
        Args.clearParam();

        File dbFolder = new File(dbPath);
        if (deleteFolder(dbFolder)) {
            logger.info("Release resources successful.");
        } else {
            logger.info("Release resources failure.");
        }
        context.destroy();
    }
}

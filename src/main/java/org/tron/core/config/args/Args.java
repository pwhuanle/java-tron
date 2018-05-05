package org.tron.core.config.args;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.common.crypto.ECKey;
import org.tron.common.overlay.discover.Node;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.config.Configuration;
import org.tron.core.config.Parameter.ChainConstant;
import org.tron.core.db.AccountStore;

@Slf4j
@NoArgsConstructor
@Component
public class Args {

  private static final Args INSTANCE = new Args();

  @Parameter(names = {"-c", "--config"}, description = "Config File")
  private String shellConfFileName = "";

  @Parameter(names = {"-d", "--output-directory"}, description = "Directory")
  private String outputDirectory = "output-directory";

  @Getter
  @Parameter(names = {"-h", "--help"}, help = true, description = "HELP message")
  private boolean help = false;

  @Getter
  @Setter
  @Parameter(names = {"-w", "--witness"})
  private boolean witness = false;

  @Getter
  @Parameter(description = "--seed-nodes")
  private List<String> seedNodes = new ArrayList<>();

  @Parameter(names = {"-p", "--private-key"}, description = "private-key")
  private String privateKey = "";

  @Parameter(names = {"--storage-directory"}, description = "Storage directory")
  private String storageDirectory = "";

  @Getter
  private Storage storage;

  @Getter
  private Overlay overlay;

  @Getter
  private SeedNode seedNode;

  @Getter
  private GenesisBlock genesisBlock;

  @Getter
  @Setter
  private String chainId;

  @Getter
  @Setter
  private LocalWitnesses localWitnesses = new LocalWitnesses();

  @Getter
  @Setter
  private boolean needSyncCheck;

  @Getter
  @Setter
  private boolean nodeDiscoveryEnable;

  @Getter
  @Setter
  private boolean nodeDiscoveryPersist;

  @Getter
  @Setter
  private int nodeConnectionTimeout;

  @Getter
  @Setter
  private List<Node> nodeActive;

  @Getter
  @Setter
  private int nodeChannelReadTimeout;

  @Getter
  @Setter
  private int nodeMaxActiveNodes;

  @Getter
  @Setter
  private int minParticipationRate;

  @Getter
  @Setter
  private int nodeListenPort;

  @Getter
  @Setter
  private String nodeDiscoveryBindIp;

  @Getter
  @Setter
  private String nodeExternalIp;

  @Getter
  @Setter
  private boolean nodeDiscoveryPublicHomeNode;

  @Getter
  @Setter
  private long nodeP2pPingInterval;

//  @Getter
//  @Setter
//  private long syncNodeCount;

  @Getter
  @Setter
  private int nodeP2pVersion;

  @Getter
  @Setter
  private String p2pNodeId;

  @Getter
  @Setter
  //If you are running a solidity node for java tron, this flag is set to true
  private boolean solidityNode = false;

  @Getter
  @Setter
  private int rpcPort;

  @Getter
  @Setter
  private long maintenanceTimeInterval; // (ms)

  @Getter
  @Setter
  @Parameter(names = {"--trust-node"}, description = "Trust node addr")
  private String trustNodeAddr;

  @Getter
  @Setter
  @Parameter(names = {"--need-replay"}, description = "need-replay")
  private boolean needReplay = false;

  @Getter
  @Setter
  @Parameter(names = {"--replay-to"}, description = "the num of block to replay")
  private long replayTo;

  public static void clearParam() {
    INSTANCE.outputDirectory = "output-directory";
    INSTANCE.help = false;
    INSTANCE.witness = false;
    INSTANCE.seedNodes = new ArrayList<>();
    INSTANCE.privateKey = "";
    INSTANCE.storageDirectory = "";
    INSTANCE.storage = null;
    INSTANCE.overlay = null;
    INSTANCE.seedNode = null;
    INSTANCE.genesisBlock = null;
    INSTANCE.chainId = null;
    INSTANCE.localWitnesses = null;
    INSTANCE.needSyncCheck = false;
    INSTANCE.nodeDiscoveryEnable = false;
    INSTANCE.nodeDiscoveryPersist = false;
    INSTANCE.nodeConnectionTimeout = 0;
    INSTANCE.nodeActive = Collections.emptyList();
    INSTANCE.nodeChannelReadTimeout = 0;
    INSTANCE.nodeMaxActiveNodes = 0;
    INSTANCE.minParticipationRate = 0;
    INSTANCE.nodeListenPort = 0;
    INSTANCE.nodeDiscoveryBindIp = "";
    INSTANCE.nodeExternalIp = "";
    INSTANCE.nodeDiscoveryPublicHomeNode = false;
    INSTANCE.nodeP2pPingInterval = 0L;
    //INSTANCE.syncNodeCount = 0;
    INSTANCE.nodeP2pVersion = 0;
    INSTANCE.rpcPort = 0;
    INSTANCE.maintenanceTimeInterval = 0;
    INSTANCE.p2pNodeId = "";
    INSTANCE.solidityNode = false;
    INSTANCE.trustNodeAddr = "";
    INSTANCE.needReplay = false;
    INSTANCE.replayTo = 0;
  }

  /**
   * set parameters.
   */
  public static void setParam(final String[] args, final String confFileName) {
    JCommander.newBuilder().addObject(INSTANCE).build().parse(args);
    Config config = Configuration.getByFileName(INSTANCE.shellConfFileName, confFileName);
    if (StringUtils.isNoneBlank(INSTANCE.privateKey)) {
      INSTANCE.setLocalWitnesses(new LocalWitnesses(INSTANCE.privateKey));
      logger.debug("Got privateKey from cmd");
    } else if (config.hasPath("localwitness")) {

      INSTANCE.localWitnesses = new LocalWitnesses();
      List<String> localwitness = config.getStringList("localwitness");
      if (localwitness.size() > 1) {
        logger.warn("localwitness size must be one, get the first one");
        localwitness = localwitness.subList(0, 1);
      }
      INSTANCE.localWitnesses.setPrivateKeys(localwitness);
      logger.debug("Got privateKey from config.conf");
    }

    if (INSTANCE.isWitness() && CollectionUtils.isEmpty(INSTANCE.localWitnesses.getPrivateKeys())) {
      logger.warn("This is a witness node,but localWitnesses is null");
    }

    INSTANCE.storage = new Storage();
    INSTANCE.storage.setDirectory(Optional.ofNullable(INSTANCE.storageDirectory)
        .filter(StringUtils::isNotEmpty)
        .orElse(config.getString("storage.directory")));
    INSTANCE.seedNode = new SeedNode();
    INSTANCE.seedNode.setIpList(Optional.ofNullable(INSTANCE.seedNodes)
        .filter(seedNode -> 0 != seedNode.size())
        .orElse(config.getStringList("seed.node.ip.list")));

    if (config.hasPath("net.type") && "mainnet".equalsIgnoreCase(config.getString("net.type"))) {
      Wallet.setAddressPreFixByte(Constant.ADD_PRE_FIX_BYTE_MAINNET);
      Wallet.setAddressPreFixString(Constant.ADD_PRE_FIX_STRING_MAINNET);
    } else {
      Wallet.setAddressPreFixByte(Constant.ADD_PRE_FIX_BYTE_TESTNET);
      Wallet.setAddressPreFixString(Constant.ADD_PRE_FIX_STRING_TESTNET);
    }

    if (config.hasPath("genesis.block")) {
      INSTANCE.genesisBlock = new GenesisBlock();

      INSTANCE.genesisBlock.setTimestamp(config.getString("genesis.block.timestamp"));
      INSTANCE.genesisBlock.setParentHash(config.getString("genesis.block.parentHash"));

      if (config.hasPath("genesis.block.assets")) {
        INSTANCE.genesisBlock.setAssets(getAccountsFromConfig(config));
        AccountStore.setAccount(config);
      }
      if (config.hasPath("genesis.block.witnesses")) {
        INSTANCE.genesisBlock.setWitnesses(getWitnessesFromConfig(config));
      }
    } else {
      INSTANCE.genesisBlock = GenesisBlock.getDefault();
    }

    INSTANCE.needSyncCheck =
        config.hasPath("block.needSyncCheck") && config.getBoolean("block.needSyncCheck");

    INSTANCE.nodeDiscoveryEnable =
        config.hasPath("node.discovery.enable") && config.getBoolean("node.discovery.enable");

    INSTANCE.nodeDiscoveryPersist =
        config.hasPath("node.discovery.persist") && config.getBoolean("node.discovery.persist");

    INSTANCE.nodeConnectionTimeout =
        config.hasPath("node.connection.timeout") ? config.getInt("node.connection.timeout") * 1000
            : 0;

    INSTANCE.nodeActive = nodeActive(config);

    INSTANCE.nodeChannelReadTimeout =
        config.hasPath("node.channel.read.timeout") ? config.getInt("node.channel.read.timeout")
            : 0;

    INSTANCE.nodeMaxActiveNodes =
        config.hasPath("node.maxActiveNodes") ? config.getInt("node.maxActiveNodes") : 0;

    INSTANCE.minParticipationRate =
        config.hasPath("node.minParticipationRate") ? config.getInt("node.minParticipationRate") : 0;

    INSTANCE.nodeListenPort =
        config.hasPath("node.listen.port") ? config.getInt("node.listen.port") : 0;

    bindIp(config);
    externalIp(config);

    INSTANCE.nodeDiscoveryPublicHomeNode =
        config.hasPath("node.discovery.public.home.node") && config
            .getBoolean("node.discovery.public.home.node");

    INSTANCE.nodeP2pPingInterval =
        config.hasPath("node.p2p.pingInterval") ? config.getLong("node.p2p.pingInterval") : 0;
//
//    INSTANCE.syncNodeCount =
//        config.hasPath("sync.node.count") ? config.getLong("sync.node.count") : 0;

    INSTANCE.nodeP2pVersion =
        config.hasPath("node.p2p.version") ? config.getInt("node.p2p.version") : 0;

    INSTANCE.rpcPort =
        config.hasPath("node.rpc.port") ? config.getInt("node.rpc.port") : 50051;

    INSTANCE.maintenanceTimeInterval =
        config.hasPath("block.maintenanceTimeInterval") ? config
            .getInt("block.maintenanceTimeInterval") : 21600000L;

    if (StringUtils.isEmpty(INSTANCE.trustNodeAddr)) {
      INSTANCE.trustNodeAddr = config.hasPath("node.trustNode") ? config.getString("node.trustNode") : null;
    }
  }


  private static List<Witness> getWitnessesFromConfig(final com.typesafe.config.Config config) {
    return config.getObjectList("genesis.block.witnesses").stream()
        .map(Args::createWitness)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private static Witness createWitness(final ConfigObject witnessAccount) {
    final Witness witness = new Witness();
    witness.setAddress(Wallet.decodeFromBase58Check(witnessAccount.get("address").unwrapped().toString()));
    witness.setUrl(witnessAccount.get("url").unwrapped().toString());
    witness.setVoteCount(witnessAccount.toConfig().getLong("voteCount"));
    return witness;
  }

  private static List<Account> getAccountsFromConfig(final com.typesafe.config.Config config) {
    return config.getObjectList("genesis.block.assets").stream()
        .map(Args::createAccount)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private static Account createAccount(final ConfigObject asset) {
    final Account account = new Account();
    account.setAccountName(asset.get("accountName").unwrapped().toString());
    account.setAccountType(asset.get("accountType").unwrapped().toString());
    account.setAddress(Wallet.decodeFromBase58Check(asset.get("address").unwrapped().toString()));
    account.setBalance(asset.get("balance").unwrapped().toString());
    return account;
  }

  public static Args getInstance() {
    return INSTANCE;
  }

  /**
   * get output directory.
   */
  public String getOutputDirectory() {
    if (!this.outputDirectory.equals("") && !this.outputDirectory.endsWith(File.separator)) {
      return this.outputDirectory + File.separator;
    }
    return this.outputDirectory;
  }

  private static List<Node> nodeActive(final com.typesafe.config.Config config) {
    if (!config.hasPath("node.active")) {
      return Collections.EMPTY_LIST;
    }
    List<Node> ret = new ArrayList<>();
    List<String> list = config.getStringList("node.active");
    for (String configString : list) {
      Node n = Node.instanceOf(configString);
      ret.add(n);
    }
    return ret;
  }

  private static void privateKey(final com.typesafe.config.Config config) {
    if (config.hasPath("private.key")) {
      INSTANCE.privateKey = config.getString("private.key");
      if (INSTANCE.privateKey.length() != ChainConstant.PRIVATE_KEY_LENGTH) {
        throw new RuntimeException(
            "The peer.privateKey needs to be Hex encoded and 32 byte length");
      }
    } else {
      INSTANCE.privateKey = getGeneratedNodePrivateKey();
    }
  }

  private static String getGeneratedNodePrivateKey() {
    String nodeId;
    try {
      File file = new File(
          INSTANCE.outputDirectory + File.separator + INSTANCE.storage.getDirectory(),
          "nodeId.properties");
      Properties props = new Properties();
      if (file.canRead()) {
        try (Reader r = new FileReader(file)) {
          props.load(r);
        }
      } else {
        ECKey key = new ECKey();
        props.setProperty("nodeIdPrivateKey", Hex.toHexString(key.getPrivKeyBytes()));
        props.setProperty("nodeId", Hex.toHexString(key.getNodeId()));
        file.getParentFile().mkdirs();
        try (Writer w = new FileWriter(file)) {
          props.store(w,
              "Generated NodeID. To use your own nodeId please refer to 'peer.privateKey' config option.");
        }
        logger.info("New nodeID generated: " + props.getProperty("nodeId"));
        logger.info("Generated nodeID and its private key stored in " + file);
      }
      nodeId = props.getProperty("nodeIdPrivateKey");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return nodeId;
  }

  private static void bindIp(final com.typesafe.config.Config config) {
    if (!config.hasPath("node.discovery.bind.ip") || config.getString("node.discovery.bind.ip")
        .trim().isEmpty()) {
      if (INSTANCE.nodeDiscoveryBindIp == null) {
        logger.info("Bind address wasn't set, Punching to identify it...");
        try {
          Socket s = new Socket("www.baidu.com", 80);
          INSTANCE.nodeDiscoveryBindIp = s.getLocalAddress().getHostAddress();
          logger.info("UDP local bound to: {}", INSTANCE.nodeDiscoveryBindIp);
        } catch (IOException e) {
          logger.warn("Can't get bind IP. Fall back to 0.0.0.0: " + e);
          INSTANCE.nodeDiscoveryBindIp = "0.0.0.0";
        }
      }
    } else {
      INSTANCE.nodeDiscoveryBindIp = config.getString("node.discovery.bind.ip").trim();
    }
  }

  private static void externalIp(final com.typesafe.config.Config config) {
    if (!config.hasPath("node.discovery.external.ip") || config
        .getString("node.discovery.external.ip").trim().isEmpty()) {
      if (INSTANCE.nodeExternalIp == null) {
        logger.info("External IP wasn't set, using checkip.amazonaws.com to identify it...");
        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(
              new URL("http://checkip.amazonaws.com").openStream()));
          INSTANCE.nodeExternalIp = in.readLine();
          if (INSTANCE.nodeExternalIp == null || INSTANCE.nodeExternalIp.trim().isEmpty()) {
            throw new IOException("Invalid address: '" + INSTANCE.nodeExternalIp + "'");
          }
          try {
            InetAddress.getByName(INSTANCE.nodeExternalIp);
          } catch (Exception e) {
            throw new IOException("Invalid address: '" + INSTANCE.nodeExternalIp + "'");
          }
          logger.info("External address identified: {}", INSTANCE.nodeExternalIp);
        } catch (IOException e) {
          INSTANCE.nodeExternalIp = INSTANCE.nodeDiscoveryBindIp;
          logger.warn(
              "Can't get external IP. Fall back to peer.bind.ip: " + INSTANCE.nodeExternalIp + " :"
                  + e);
        }
      }
    } else {
      INSTANCE.nodeExternalIp = config.getString("node.discovery.external.ip").trim();
    }
  }

  public ECKey getMyKey() {
    if (StringUtils.isEmpty(INSTANCE.p2pNodeId)) {
      INSTANCE.p2pNodeId = getGeneratedNodePrivateKey();
    }

    return ECKey.fromPrivate(Hex.decode(INSTANCE.p2pNodeId));
  }
}

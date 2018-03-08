package org.tron.core.config.args;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.ConfigObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.ConfigObject;
import org.tron.core.config.args.InitialWitness.LocalWitness;

public class Args {

  private static final Logger logger = LoggerFactory.getLogger("Args");

  private static final Args INSTANCE = new Args();

  @Parameter(names = {"-d", "--output-directory"}, description = "Directory")
  private String outputDirectory = "output-directory";

  @Parameter(names = {"-h", "--help"}, help = true, description = "Directory")
  private boolean help = false;

  @Parameter(description = "--seed-nodes")
  private List<String> seedNodes = new ArrayList<>();

  @Parameter(names = {"-p", "--private-key"}, description = "private-key")
  private String privateKey = "";

  @Parameter(names = {"--storage-directory"}, description = "Storage directory")
  private String storageDirectory = "";

  @Parameter(names = {"--overlay-port"}, description = "Overlay port")
  private int overlayPort = 0;

  private Storage storage;
  private Overlay overlay;
  private SeedNode seedNode;
  private GenesisBlock genesisBlock;
  private String chainId;
  private InitialWitness initialWitness;

  private Args() {

  }

  /**
   * set parameters.
   */
  public static void setParam(String[] args, com.typesafe.config.Config config) {
    JCommander.newBuilder().addObject(INSTANCE).build().parse(args);

    if (StringUtils.isBlank(INSTANCE.privateKey)) {
      if (config.hasPath("private.key")) {
        INSTANCE.privateKey = config.getString("private.key");
      }
    }
    logger.info("private.key = {}", INSTANCE.privateKey);

    INSTANCE.storage = new Storage();
    INSTANCE.storage.setDirectory(config.getString("storage.directory"));
    if (!INSTANCE.storageDirectory.isEmpty()) {
      INSTANCE.storage.setDirectory(INSTANCE.storageDirectory);
    }

    INSTANCE.overlay = new Overlay();
    INSTANCE.overlay.setPort(config.getInt("overlay.port"));
    if (INSTANCE.overlayPort != 0) {
      INSTANCE.overlay.setPort(INSTANCE.overlayPort);
    }

    INSTANCE.seedNode = new SeedNode();
    INSTANCE.seedNode.setIpList(config.getStringList("seed.node.ip.list"));
    if (INSTANCE.seedNodes.size() != 0) {
      INSTANCE.seedNode.setIpList(INSTANCE.seedNodes);
    }

    if (config.hasPath("genesis.block")) {
      INSTANCE.genesisBlock = new GenesisBlock();

      INSTANCE.genesisBlock.setTimeStamp(config.getString("genesis.block.timestamp"));
      INSTANCE.genesisBlock.setParentHash(config.getString("genesis.block.parentHash"));
      INSTANCE.genesisBlock.setHash(config.getString("genesis.block.hash"));
      INSTANCE.genesisBlock.setNumber(config.getString("genesis.block.number"));

      if (config.hasPath("genesis.block.assets")) {
        List<Account> accounts = getAccountsFromConfig(config);

        INSTANCE.genesisBlock.setAssets(accounts);
      }
    } else {
      INSTANCE.genesisBlock = GenesisBlock.getDefault();
    }

    if (config.hasPath("initialWitness")) {
      INSTANCE.initialWitness = new InitialWitness();

      if (config.hasPath("initialWitness.localWitness")) {
        INSTANCE.initialWitness.setLocalWitness(getLocalWitnessFromConfig(config));
      }

      if (config.hasPath("initialWitness.activeWitness")) {
        INSTANCE.initialWitness.setActiveWitnessList(getActiveWitnessFromConfig(config));
      }
    } else {
      INSTANCE.initialWitness = new InitialWitness();
    }
  }

  private static List<Account> getAccountsFromConfig(com.typesafe.config.Config config) {
    List<? extends ConfigObject> assets = config.getObjectList("genesis.block.assets");
    return assets.stream()
            .map(Args::createAccount)
            .collect(Collectors.toCollection(ArrayList::new));
  }

  private static Account createAccount(ConfigObject asset) {
    Account account = new Account();
    account.setAddress(asset.get("address").unwrapped().toString());
    account.setBalance(asset.get("balance").unwrapped().toString());
    return account;
  }

  private static InitialWitness.LocalWitness getLocalWitnessFromConfig(
      com.typesafe.config.Config config) {

    InitialWitness.LocalWitness localWitness = new InitialWitness.LocalWitness();
    localWitness.setPrivateKey(config.getString("initialWitness.localWitness.privateKey"));
    localWitness.setUrl(config.getString("initialWitness.localWitness.url"));
    return localWitness;
  }

  private static List<InitialWitness.ActiveWitness> getActiveWitnessFromConfig(
      com.typesafe.config.Config config) {
    List<? extends ConfigObject> objectList = config.getObjectList("initialWitness.activeWitness");

    List<InitialWitness.ActiveWitness> activeWitnessList = new ArrayList<>();
    objectList.forEach(object -> activeWitnessList.add(createActiveWitness(object)));
    return activeWitnessList;
  }

  private static InitialWitness.ActiveWitness createActiveWitness(ConfigObject asset) {
    InitialWitness.ActiveWitness activeWitness = new InitialWitness.ActiveWitness();
    activeWitness.setPublicKey(asset.get("publicKey").unwrapped().toString());
    activeWitness.setUrl(asset.get("url").unwrapped().toString());
    return activeWitness;
  }

  public static Args getInstance() {
    return INSTANCE;
  }

  /**
   * get output directory.
   */
  public String getOutputDirectory() {
    if (!outputDirectory.equals("") && !outputDirectory.endsWith(File.separator)) {
      return outputDirectory + File.separator;
    }
    return outputDirectory;
  }

  public boolean isHelp() {
    return help;
  }

  public List<String> getSeedNodes() {
    return seedNodes;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public Storage getStorage() {
    return storage;
  }

  public Overlay getOverlay() {
    return overlay;
  }

  public SeedNode getSeedNode() {
    return seedNode;
  }

  public GenesisBlock getGenesisBlock() {
    return genesisBlock;
  }

  public String getChainId() {
    return chainId;
  }

  public void setChainId(String chainId) {
    this.chainId = chainId;
  }

  public InitialWitness getInitialWitness() {
    return initialWitness;
  }

  public void setInitialWitness(InitialWitness initialWitness) {
    this.initialWitness = initialWitness;
  }


}

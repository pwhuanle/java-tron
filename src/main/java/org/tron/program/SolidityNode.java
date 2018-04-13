package org.tron.program;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.StringUtils;
import org.tron.common.application.Application;
import org.tron.common.application.ApplicationFactory;
import org.tron.common.overlay.client.DatabaseGrpcClient;
import org.tron.core.Constant;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.core.exception.*;
import org.tron.core.services.RpcApiService;
import org.tron.protos.Protocol.DynamicProperties;
import org.tron.protos.Protocol.Block;

@Slf4j
public class SolidityNode {

  private DatabaseGrpcClient databaseGrpcClient;
  private Manager dbManager;

  public void setDbManager(Manager dbManager) {
    this.dbManager = dbManager;
  }

  private void initGrpcClient(String addr) {
    try {
      databaseGrpcClient = new DatabaseGrpcClient(addr);
    } catch (Exception e) {
      logger.error("Failed to create database grpc client {}", addr);
      System.exit(0);
    }
  }

  private void syncLoop(Args args) {
    while (true) {
      try {
        initGrpcClient(args.getTrustNodeAddr());
        syncSolidityBlock();
      } catch (Exception e) {
        logger.error("Error in sync solidity block {}", e.getMessage());
      }
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void syncSolidityBlock() throws BadBlockException {
    while (true) {
      DynamicProperties remoteDynamicProperties = databaseGrpcClient.getDynamicProperties();
      long remoteLastSolidityBlockNum = remoteDynamicProperties.getLastSolidityBlockNum();
      long lastSolidityBlockNum = dbManager.getDynamicPropertiesStore()
          .getLatestSolidifiedBlockNum();
      if (lastSolidityBlockNum < remoteLastSolidityBlockNum) {
        Block block = databaseGrpcClient.getBlock(lastSolidityBlockNum + 1);
        try {
          BlockCapsule blockCapsule = new BlockCapsule(block);
          dbManager.pushBlock(blockCapsule);
          dbManager.getDynamicPropertiesStore()
              .saveLatestSolidifiedBlockNum(lastSolidityBlockNum + 1);
        } catch (ValidateScheduleException e) {
          throw new BadBlockException("validate schedule exception");
        } catch (ValidateSignatureException e) {
          throw new BadBlockException("validate signature exception");
        } catch (ContractValidateException e) {
          throw new BadBlockException("ContractValidate exception");
        } catch (ContractExeException | UnLinkedBlockException e) {
          throw new BadBlockException("Contract Exectute exception");
        }
      } else {
        break;
      }
    }
    logger.info("Sync with trust node completed!!!");
  }

  private void start(Args cfgArgs) {
    new Thread(() -> syncLoop(cfgArgs), logger.getName()).start();
  }

  /**
   * Start the SolidityNode.
   */
  public static void main(String[] args) throws InterruptedException {
    logger.info("Solidity node running.");
    Args.setParam(args, Constant.TESTNET_CONF);
    Args cfgArgs = Args.getInstance();

    if (StringUtils.isEmpty(cfgArgs.getTrustNodeAddr())) {
      logger.error("Trust node not set.");
      return;
    }
    cfgArgs.setSolidityNode(true);

    ApplicationContext context = new AnnotationConfigApplicationContext(DefaultConfig.class);

    if (cfgArgs.isHelp()) {
      logger.info("Here is the help message.");
      return;
    }
    Application appT = ApplicationFactory.create(context);
    //appT.init(cfgArgs);
    RpcApiService rpcApiService = new RpcApiService(appT, context);
    appT.addService(rpcApiService);

    appT.initServices(cfgArgs);
    appT.startServices();
//    appT.startup();

    SolidityNode node = new SolidityNode();
    node.setDbManager(appT.getDbManager());
    node.start(cfgArgs);

    rpcApiService.blockUntilShutdown();
  }
}

package org.tron.program;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.application.Application;
import org.tron.common.application.ApplicationFactory;
import org.tron.core.Constant;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.services.RpcApiService;
import org.tron.core.services.WitnessService;

@Slf4j
public class FullNode {

  /**
   * Start the FullNode.
   */
  public static void main(String[] args) throws InterruptedException {
    logger.info("Full node running.");
    Args.setParam(args, Constant.NORMAL_CONF);
    Args cfgArgs = Args.getInstance();

    ApplicationContext context = new AnnotationConfigApplicationContext(DefaultConfig.class);

    if (cfgArgs.isHelp()) {
      logger.info("Here is the help message.");
      return;
    }
    Application appT = ApplicationFactory.create(context);
    RpcApiService rpcApiService = new RpcApiService(appT);
    appT.addService(rpcApiService);
    if (cfgArgs.isWitness()) {
      appT.addService(new WitnessService(appT));
    }
    appT.initServices(cfgArgs);
    appT.startServices();
    appT.startup();

    rpcApiService.blockUntilShutdown();
  }
}

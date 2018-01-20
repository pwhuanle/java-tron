package org.tron.core.services;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;
import org.tron.api.GrpcAPI;
import org.tron.common.application.Service;

public class RpcApiService implements Service {

  private static final Logger logger = Logger.getLogger(RpcApiService.class.getName());
  private int port = 50051;
  private Server ApiServer;

  @Override
  public void start() {
    try {
      ApiServer = ServerBuilder.forPort(10086)
          .addService(new WalletApi())
          .build()
          .start();
    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.info("Server started, listening on " + port);

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {

        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        //server.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private class WalletApi extends org.tron.api.WalletGrpc.WalletImplBase {

    @Override
    public void getBalance(GrpcAPI.Balance req, StreamObserver<GrpcAPI.Balance> responseObserver) {

    }
  }

  @Override
  public void stop() {

  }
}

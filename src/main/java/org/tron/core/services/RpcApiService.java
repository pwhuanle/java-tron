package org.tron.core.services;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.tron.api.DatabaseGrpc.DatabaseImplBase;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountList;
import org.tron.api.GrpcAPI.Address;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockReference;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.Node;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.api.WalletGrpc.WalletImplBase;
import org.tron.common.application.Application;
import org.tron.common.application.Service;
import org.tron.common.overlay.discover.NodeHandler;
import org.tron.common.overlay.discover.NodeManager;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.WitnessCapsule;
import org.tron.core.config.args.Args;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AccountCreateContract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Contract.ParticipateAssetIssueContract;
import org.tron.protos.Contract.TransferAssetContract;
import org.tron.protos.Contract.TransferContract;
import org.tron.protos.Contract.VoteWitnessContract;
import org.tron.protos.Contract.WitnessCreateContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.DynamicProperties;
import org.tron.protos.Protocol.Transaction;


@Slf4j
public class RpcApiService implements Service {

  private int port = 50051;
  private Server apiServer;
  private Application app;

  private NodeManager nodeManager;

  public RpcApiService(Application app, ApplicationContext ctx) {
    nodeManager = ctx.getBean(NodeManager.class);
    this.app = app;
  }

  @Override
  public void init() {

  }

  @Override
  public void init(Args args) {

  }

  @Override
  public void start() {
    try {
      apiServer = ServerBuilder.forPort(port)
          .addService(new WalletApi(app))
          .addService(new DatabaseApi(app))
          .build()
          .start();
    } catch (IOException e) {
      logger.debug(e.getMessage(), e);
    }

    logger.info("Server started, listening on " + port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      //server.this.stop();
      System.err.println("*** server shut down");
    }));
  }


  private class DatabaseApi extends DatabaseImplBase {

    private Application app;

    private DatabaseApi(Application app) {
      this.app = app;
    }

    @Override
    public void getBlockReference(org.tron.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.api.GrpcAPI.BlockReference> responseObserver) {
      long headBlockNum = app.getDbManager().getDynamicPropertiesStore()
          .getLatestBlockHeaderNumber();
      byte[] blockHeaderHash = app.getDbManager().getDynamicPropertiesStore()
          .getLatestBlockHeaderHash().getBytes();
      BlockReference ref = BlockReference.newBuilder()
          .setBlockHash(ByteString.copyFrom(blockHeaderHash))
          .setBlockNum(headBlockNum)
          .build();
      responseObserver.onNext(ref);
      responseObserver.onCompleted();
    }

    @Override
    public void getNowBlock(EmptyMessage request, StreamObserver<Block> responseObserver) {
      Sha256Hash headBlockId = app.getDbManager().getHeadBlockId();
      Block block = null;
      try {
        block = app.getDbManager().getBlockById(headBlockId).getInstance();
      } catch (BadItemException e) {
      } catch (ItemNotFoundException e) {
      }
      responseObserver.onNext(block);
      responseObserver.onCompleted();
    }

    @Override
    public void getBlockByNum(NumberMessage request, StreamObserver<Block> responseObserver) {
      Sha256Hash headBlockId = null;
      try {
        headBlockId = app.getDbManager().getBlockIdByNum(request.getNum());
      } catch (BadItemException e) {
        e.printStackTrace();
      } catch (ItemNotFoundException e) {
        e.printStackTrace();
      }
      Block block = null;
      try {
        block = app.getDbManager().getBlockById(headBlockId).getInstance();
      } catch (BadItemException e) {
      } catch (ItemNotFoundException e) {
      }
      responseObserver.onNext(block);
      responseObserver.onCompleted();
    }

    @Override
    public void getDynamicProperties(EmptyMessage request,
        StreamObserver<DynamicProperties> responseObserver) {
      DynamicProperties.Builder builder = DynamicProperties.newBuilder();
      builder.setLastSolidityBlockNum(
          app.getDbManager().getDynamicPropertiesStore().getLatestSolidifiedBlockNum());
      DynamicProperties dynamicProperties = builder.build();
      responseObserver.onNext(dynamicProperties);
      responseObserver.onCompleted();
    }
  }

  private class WalletApi extends WalletImplBase {

    private Application app;
    private Wallet wallet;

    private WalletApi(Application app) {
      this.app = app;
      this.wallet = new Wallet(this.app);
    }


    @Override
    public void getAccount(Account req, StreamObserver<Account> responseObserver) {
      ByteString addressBs = req.getAddress();
      if (addressBs != null) {
        //      byte[] addressBa = addressBs.toByteArray();
        //     long balance = wallet.getBalance(addressBa);
        //    Account reply = Account.newBuilder().setBalance(balance).build();
        Account reply = wallet.getBalance(req);
        responseObserver.onNext(reply);
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void createTransaction(TransferContract req,
        StreamObserver<Transaction> responseObserver) {
      ByteString fromBs = req.getOwnerAddress();
      ByteString toBs = req.getToAddress();
      long amount = req.getAmount();
      if (fromBs != null && toBs != null && amount > 0) {
        Transaction trx = wallet.createTransaction(req);
        responseObserver.onNext(trx);
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void broadcastTransaction(Transaction req,
        StreamObserver<GrpcAPI.Return> responseObserver) {
      boolean ret = wallet.broadcastTransaction(req);
      GrpcAPI.Return retur = GrpcAPI.Return.newBuilder().setResult(ret).build();
      responseObserver.onNext(retur);
      responseObserver.onCompleted();
    }

    @Override
    public void createAccount(AccountCreateContract request,
        StreamObserver<Transaction> responseObserver) {
      if (request.getType() == null || request.getAccountName() == null
          || request.getOwnerAddress() == null) {
        responseObserver.onNext(null);
      } else {
        Transaction trx = wallet.createAccount(request);
        responseObserver.onNext(trx);
      }
      responseObserver.onCompleted();
    }


    @Override
    public void createAssetIssue(AssetIssueContract request,
        StreamObserver<Transaction> responseObserver) {
      ByteString owner = request.getOwnerAddress();
      if (owner != null) {
        Transaction trx = wallet.createTransaction(request);
        responseObserver.onNext(trx);
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    //refactor、test later
    private void checkVoteWitnessAccount(VoteWitnessContract req) {
      //send back to cli
      ByteString ownerAddress = req.getOwnerAddress();
      Preconditions.checkNotNull(ownerAddress, "OwnerAddress is null");

      AccountCapsule account = app.getDbManager().getAccountStore().get(ownerAddress.toByteArray());
      Preconditions.checkNotNull(account, "OwnerAddress[" + ownerAddress + "] not exists");

      int votesCount = req.getVotesCount();
      Preconditions.checkArgument(votesCount <= 0, "VotesCount[" + votesCount + "] <= 0");
      Preconditions.checkArgument(
          account.getShare() < votesCount,
          "Share[" + account.getShare() + "] <  VotesCount[" + votesCount + "]");

      req.getVotesList().forEach(vote -> {
        ByteString voteAddress = vote.getVoteAddress();
        WitnessCapsule witness = app.getDbManager().getWitnessStore()
            .get(voteAddress.toByteArray());
        Preconditions.checkNotNull(witness, "witness[" + voteAddress + "] not exists");
        Preconditions.checkArgument(
            vote.getVoteCount() <= 0,
            "VoteAddress[" + voteAddress + "],VotesCount[" + vote.getVoteCount() + "] <= 0");
      });
    }

    @Override
    public void voteWitnessAccount(VoteWitnessContract req,
        StreamObserver<Transaction> response) {

      try {
//        checkVoteWitnessAccount(req);//to be complemented later
        Transaction trx = wallet.createTransaction(req);
        response.onNext(trx);
      } catch (Exception ex) {
        response.onNext(null);
      }
      response.onCompleted();
    }

    @Override
    public void createWitness(WitnessCreateContract req,
        StreamObserver<Transaction> responseObserver) {
      ByteString fromBs = req.getOwnerAddress();

      if (fromBs != null) {
        Transaction trx = wallet.createTransaction(req);
        responseObserver.onNext(trx);
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void updateWitness(Contract.WitnessUpdateContract req,
        StreamObserver<Transaction> responseObserver) {
      if (req.getOwnerAddress() != null) {
        Transaction trx = wallet.createTransaction(req);
        responseObserver.onNext(trx);
      } else {
        responseObserver.onNext(null);
      }

      responseObserver.onCompleted();
    }

    @Override
    public void getNowBlock(EmptyMessage request, StreamObserver<Block> responseObserver) {
      responseObserver.onNext(wallet.getNowBlock());
      responseObserver.onCompleted();
    }

    @Override
    public void getBlockByNum(NumberMessage request, StreamObserver<Block> responseObserver) {
      responseObserver.onNext(wallet.getBlockByNum(request.getNum()));
      responseObserver.onCompleted();
    }

    @Override
    public void listAccounts(EmptyMessage request, StreamObserver<AccountList> responseObserver) {
      responseObserver.onNext(wallet.getAllAccounts());
      responseObserver.onCompleted();
    }

    @Override
    public void listWitnesses(EmptyMessage request, StreamObserver<WitnessList> responseObserver) {
      responseObserver.onNext(wallet.getWitnessList());
      responseObserver.onCompleted();
    }

    @Override
    public void listNodes(EmptyMessage request, StreamObserver<NodeList> responseObserver) {
      List<NodeHandler> handlerList = nodeManager.dumpActiveNodes();

      Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();
      for (NodeHandler handler : handlerList) {
        String key = handler.getNode().getHexId() + handler.getNode().getHost();
        nodeHandlerMap.put(key, handler);
      }

      NodeList.Builder nodeListBuilder = NodeList.newBuilder();

      nodeHandlerMap.entrySet().stream()
          .forEach(v -> {
            org.tron.common.overlay.discover.Node node = v.getValue().getNode();
            nodeListBuilder.addNodes(Node.newBuilder().setAddress(
                Address.newBuilder()
                    .setHost(ByteString.copyFrom(ByteArray.fromString(node.getHost())))
                    .setPort(node.getPort())));
          });

      responseObserver.onNext(nodeListBuilder.build());
      responseObserver.onCompleted();
    }

    @Override
    public void transferAsset(TransferAssetContract request,
        StreamObserver<Transaction> responseObserver) {
      ByteString fromBs = request.getOwnerAddress();

      if (fromBs != null) {
        Transaction trx = wallet.createTransaction(request);
        responseObserver.onNext(trx);
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void participateAssetIssue(ParticipateAssetIssueContract request,
        StreamObserver<Transaction> responseObserver) {
      ByteString fromBs = request.getOwnerAddress();

      if (fromBs != null) {
        Transaction trx = wallet.createTransaction(request);
        responseObserver.onNext(trx);
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void getAssetIssueList(EmptyMessage request,
        StreamObserver<AssetIssueList> responseObserver) {
      responseObserver.onNext(wallet.getAssetIssueList());
      responseObserver.onCompleted();
    }

    @Override
    public void getAssetIssueByAccount(Account request,
        StreamObserver<AssetIssueList> responseObserver) {
      ByteString fromBs = request.getAddress();

      if (fromBs != null) {
        responseObserver.onNext(wallet.getAssetIssueByAccount(fromBs));
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void getAssetIssueByName(BytesMessage request,
        StreamObserver<AssetIssueContract> responseObserver) {
      ByteString asertName = request.getValue();

      if (asertName != null) {
        responseObserver.onNext(wallet.getAssetIssueByName(asertName));
      } else {
        responseObserver.onNext(null);
      }
      responseObserver.onCompleted();
    }

    @Override
    public void totalTransaction(EmptyMessage request,
        StreamObserver<NumberMessage> responseObserver) {
      responseObserver.onNext(wallet.totalTransaction());
      responseObserver.onCompleted();
    }

  }

  @Override
  public void stop() {

  }

  /**
   * ...
   */
  public void blockUntilShutdown() {
    if (apiServer != null) {
      try {
        apiServer.awaitTermination();
      } catch (InterruptedException e) {
        logger.debug(e.getMessage(), e);
      }
    }
  }
}

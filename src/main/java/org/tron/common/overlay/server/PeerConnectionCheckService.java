package org.tron.common.overlay.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.common.overlay.discover.node.NodeStatistics;
import org.tron.core.config.args.Args;
import org.tron.core.net.peer.PeerConnection;
import org.tron.protos.Protocol.ReasonCode;

@Slf4j
@Service
public class PeerConnectionCheckService {

  public static final long CHECK_TIME = 5 * 60 * 1000L;
  private double disconnectNumberFactor = Args.getInstance().getDisconnectNumberFactor();
  private double maxConnectNumberFactor = Args.getInstance().getMaxConnectNumberFactor();

  @Autowired
  private SyncPool pool;
  
  @Autowired
  private ChannelManager channelManager;

  private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1,
      r -> new Thread(r, "check-peer-connect"));

  @PostConstruct
  public void check() {
    logger.info("start the PeerConnectionCheckService");
    scheduledExecutorService
        .scheduleWithFixedDelay(new CheckDataTransferTask(), 5, 5, TimeUnit.MINUTES);
  }

  @PreDestroy
  public void destroy() {
    scheduledExecutorService.shutdown();
  }

  private class CheckDataTransferTask implements Runnable {

    @Override
    public void run() {
      List<PeerConnection> peerConnectionList = pool.getActivePeers();
      List<Channel> willDisconnectPeerList = new ArrayList<>();
      for (PeerConnection peerConnection : peerConnectionList) {
        NodeStatistics nodeStatistics = peerConnection.getNodeStatistics();
        if (!nodeStatistics.nodeIsHaveDataTransfer()
            && System.currentTimeMillis() - peerConnection.getStartTime() >= CHECK_TIME
            && !channelManager.getTrustPeers().containsKey(peerConnection.getInetAddress())
            && !nodeStatistics.isPredefined()) {
          //&& !peerConnection.isActive()
          //if xxx minutes not have data transfer,disconnect the peer,exclude trust peer and active peer
          willDisconnectPeerList.add(peerConnection);
        }
        nodeStatistics.resetTcpFlow();
      }
      if (!willDisconnectPeerList.isEmpty() && peerConnectionList.size()
          > Args.getInstance().getNodeMaxActiveNodes() * maxConnectNumberFactor) {
        Collections.shuffle(willDisconnectPeerList);
        for (int i = 0; i < willDisconnectPeerList.size() * disconnectNumberFactor; i++) {
          logger.error("{} not have data transfer, disconnect the peer",
              willDisconnectPeerList.get(i).getInetAddress());
          willDisconnectPeerList.get(i).disconnect(ReasonCode.TOO_MANY_PEERS);
        }
      }
    }
  }

}

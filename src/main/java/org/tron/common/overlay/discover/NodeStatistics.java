/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.common.overlay.discover;

import static java.lang.Math.min;

import java.util.concurrent.atomic.AtomicLong;
import org.tron.common.overlay.message.ReasonCode;

public class NodeStatistics {

  public final static int REPUTATION_PREDEFINED = 1000500;
  public final static long TOO_MANY_PEERS_PENALIZE_TIMEOUT = 10 * 1000;

  public class StatHandler {

    AtomicLong count = new AtomicLong(0);

    public void add() {
      count.incrementAndGet();
    }

    public void add(long delta) {
      count.addAndGet(delta);
    }

    public long get() {
      return count.get();
    }

    public String toString() {
      return count.toString();
    }
  }

  private final Node node;

  private boolean isPredefined = false;

  private int persistedReputation = 0;

  // discovery stat
  public final StatHandler discoverOutPing = new StatHandler();
  public final StatHandler discoverInPong = new StatHandler();
  public final StatHandler discoverOutPong = new StatHandler();
  public final StatHandler discoverInPing = new StatHandler();
  public final StatHandler discoverInFind = new StatHandler();
  public final StatHandler discoverOutFind = new StatHandler();
  public final StatHandler discoverInNeighbours = new StatHandler();
  public final StatHandler discoverOutNeighbours = new StatHandler();
  public final SimpleStatter discoverMessageLatency;
  public final AtomicLong lastPongReplyTime = new AtomicLong(0l); // in milliseconds

  // rlpx stat
  public final StatHandler p2pOutHello = new StatHandler();
  public final StatHandler p2pInHello = new StatHandler();
  public final StatHandler p2pHandShake = new StatHandler();
  public final StatHandler tronOutMessage = new StatHandler();
  public final StatHandler tronInMessage = new StatHandler();
  // Not the fork we are working on
  // Set only after specific block hashes received
  //public boolean wrongFork;

  private String clientId = "";

  private ReasonCode tronLastRemoteDisconnectReason = null;
  private ReasonCode tronLastLocalDisconnectReason = null;
  private long lastDisconnectedTime = 0;


  public NodeStatistics(Node node) {
    this.node = node;
    discoverMessageLatency = new SimpleStatter(node.getId().toString());
  }

  private int getSessionReputation() {
    return getSessionFairReputation() + (isPredefined ? REPUTATION_PREDEFINED : 0);
  }

  private int getSessionFairReputation() {
    int discoverReput = 0;

    discoverReput +=
        min(discoverInPong.get(), 50) * (discoverOutPing.get() == discoverInPong.get() ? 2 : 1);
    discoverReput += min(discoverInNeighbours.get(), 50) * 2;
    //discoverReput += 20 / (min((int)discoverMessageLatency.getAvrg(), 1) / 100);

    int rlpxReput = 0;
    rlpxReput += p2pHandShake.get() > 0 ? 20 : 0;
    rlpxReput += min(tronInMessage.get(), 10) * 3;

    if (wasDisconnected()) {
      if (tronLastLocalDisconnectReason == null && tronLastRemoteDisconnectReason == null) {
        // means connection was dropped without reporting any reason - bad
        rlpxReput *= 0.3;
      } else if (tronLastLocalDisconnectReason != ReasonCode.REQUESTED) {
        // the disconnect was not initiated by discover mode
        if (tronLastRemoteDisconnectReason == ReasonCode.TOO_MANY_PEERS) {
          // The peer is popular, but we were unlucky
          rlpxReput *= 0.3;
        } else if (tronLastRemoteDisconnectReason != ReasonCode.REQUESTED) {
          // other disconnect reasons
          rlpxReput *= 0.2;
        }
      }
    }
    return discoverReput + 100 * rlpxReput;
  }

  public int getReputation() {
    return isReputationPenalized() ? 0 : persistedReputation / 2 + getSessionReputation();
  }

  private boolean isReputationPenalized() {
//    if (wrongFork) {
//      return true;
//    }
    if (wasDisconnected() && tronLastRemoteDisconnectReason == ReasonCode.TOO_MANY_PEERS &&
        System.currentTimeMillis() - lastDisconnectedTime < TOO_MANY_PEERS_PENALIZE_TIMEOUT) {
      return true;
    }
    if (wasDisconnected() && tronLastRemoteDisconnectReason == ReasonCode.DUPLICATE_PEER &&
        System.currentTimeMillis() - lastDisconnectedTime < TOO_MANY_PEERS_PENALIZE_TIMEOUT) {
      return true;
    }
    return tronLastLocalDisconnectReason == ReasonCode.NULL_IDENTITY ||
        tronLastRemoteDisconnectReason == ReasonCode.NULL_IDENTITY ||
        tronLastLocalDisconnectReason == ReasonCode.INCOMPATIBLE_PROTOCOL ||
        tronLastRemoteDisconnectReason == ReasonCode.INCOMPATIBLE_PROTOCOL ||
        tronLastLocalDisconnectReason == ReasonCode.USELESS_PEER ||
        tronLastRemoteDisconnectReason == ReasonCode.USELESS_PEER ||
        tronLastLocalDisconnectReason == ReasonCode.BAD_PROTOCOL ||
        tronLastRemoteDisconnectReason == ReasonCode.BAD_PROTOCOL;
  }

  public void nodeDisconnectedRemote(ReasonCode reason) {
    lastDisconnectedTime = System.currentTimeMillis();
    tronLastRemoteDisconnectReason = reason;
  }

  public void nodeDisconnectedLocal(ReasonCode reason) {
    lastDisconnectedTime = System.currentTimeMillis();
    tronLastLocalDisconnectReason = reason;
  }

  public boolean wasDisconnected() {
    return lastDisconnectedTime > 0;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setPredefined(boolean isPredefined) {
    this.isPredefined = isPredefined;
  }

  public boolean isPredefined() {
    return isPredefined;
  }

//  private String getStatName() {
//    return "ethj.discover.nodes." + node.getHost() + ":" + node.getPort();
//  }

  public int getPersistedReputation() {
    return isReputationPenalized() ? 0 : (persistedReputation + getSessionFairReputation()) / 2;
  }

  public void setPersistedReputation(int persistedReputation) {
    this.persistedReputation = persistedReputation;
  }

  @Override
  public String toString() {
    return "NodeStat[reput: " + getReputation() + "(" + persistedReputation + "), discover: " +
        discoverInPong + "/" + discoverOutPing + " " +
        discoverOutPong + "/" + discoverInPing + " " +
        discoverInNeighbours + "/" + discoverOutFind + " " +
        discoverOutNeighbours + "/" + discoverInFind + " " +
        ((int) discoverMessageLatency.getAvrg()) + "ms" +
        ", p2p: " + p2pHandShake + "/" + p2pInHello + "/" + p2pOutHello + " " +
        ", tron: " + tronInMessage + "/" + tronOutMessage + " " +
        (wasDisconnected() ? "X " : "") +
        (tronLastLocalDisconnectReason != null ? ("<=" + tronLastLocalDisconnectReason) : " ") +
        (tronLastRemoteDisconnectReason != null ? ("=>" + tronLastRemoteDisconnectReason) : " ") +
        "[" + clientId + "]";
  }

  public class SimpleStatter {

    private final String name;
    private volatile double last;
    private volatile double sum;
    private volatile int count;

    public SimpleStatter(String name) {
      this.name = name;
    }

    public void add(double value) {
      last = value;
      sum += value;
      count++;
    }

    public double getLast() {
      return last;
    }

    public int getCount() {
      return count;
    }

    public double getSum() {
      return sum;
    }

    public double getAvrg() {
      return getSum() / getCount();
    }

    public String getName() {
      return name;
    }

  }

}

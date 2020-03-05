package org.tron.common.overlay.discover.node;

import java.io.Serializable;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.option.KademliaOptions;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;

@Slf4j(topic = "discover")
public class Node implements Serializable {

  private static final long serialVersionUID = -4267600517925770636L;

  private byte[] id;

  private String host;

  private int port;

  @Getter
  private int bindPort;

  @Setter
  private int p2pVersion;

  private boolean isFakeNodeId = false;

  public Node(byte[] id, String host, int port) {
    this.id = id;
    this.host = host;
    this.port = port;
    this.isFakeNodeId = true;
  }

  public Node(byte[] id, String host, int port, int bindPort) {
    this.id = id;
    this.host = host;
    this.port = port;
    this.bindPort = bindPort;
  }

  public static Node instanceOf(String hostPort) {
    try {
      String[] sz = hostPort.split(":");
      int port = Integer.parseInt(sz[1]);
      return new Node(Node.getNodeId(), sz[0], port);
    } catch (Exception e) {
      logger.error("Parse node failed, {}", hostPort);
      throw e;
    }
  }

  public static byte[] getNodeId() {
    Random gen = new Random();
    byte[] id = new byte[KademliaOptions.NODE_ID_LEN];
    gen.nextBytes(id);
    return id;
  }

  public boolean isConnectible(int argsP2pversion) {
    return port == bindPort && p2pVersion == argsP2pversion;
  }

  public String getHexId() {
    return Hex.toHexString(id);
  }

  public String getHexIdShort() {
    return Utils.getIdShort(getHexId());
  }

  public boolean isDiscoveryNode() {
    return isFakeNodeId;
  }

  public byte[] getId() {
    return id;
  }

  public void setId(byte[] id) {
    this.id = id;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getIdString() {
    if (id == null) {
      return null;
    }
    return new String(id);
  }

  @Override
  public String toString() {
    return "Node{" + " host='" + host + '\'' + ", port=" + port
        + ", id=" + ByteArray.toHexString(id) + '}';
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    if (o == this) {
      return true;
    }

    if (o.getClass() == getClass()) {
      return StringUtils.equals(getIdString(), ((Node) o).getIdString());
    }

    return false;
  }
}

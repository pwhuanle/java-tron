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

import static org.tron.common.crypto.Hash.sha3;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;

public class Node implements Serializable {

  private static final long serialVersionUID = -4267600517925770636L;

  private byte[] id;

  private String host;

  private int port;

  private boolean isFakeNodeId = false;

  public int getReputation() {
    return reputation;
  }

  public void setReputation(int reputation) {
    this.reputation = reputation;
  }

  private int reputation = 0;

  public static Node instanceOf(String addressOrEnode) {
    try {
      URI uri = new URI(addressOrEnode);
      if (uri.getScheme().equals("enode")) {
        return new Node(addressOrEnode);
      }
    } catch (URISyntaxException e) {
      // continue
    }

    final ECKey generatedNodeKey = ECKey.fromPrivate(sha3(addressOrEnode.getBytes()));
    final String generatedNodeId = Hex.toHexString(generatedNodeKey.getNodeId());
    final Node node = new Node("enode://" + generatedNodeId + "@" + addressOrEnode);
    node.isFakeNodeId = true;
    return node;
  }

  public String getEnodeURL() {
    return new StringBuilder("enode://")
        .append(ByteArray.toHexString(id)).append("@")
        .append(host).append(":")
        .append(port).toString();
  }

  public Node(String enodeURL) {
    try {
      URI uri = new URI(enodeURL);
      if (!uri.getScheme().equals("enode")) {
        throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT");
      }
      this.id = Hex.decode(uri.getUserInfo());
      this.host = uri.getHost();
      this.port = uri.getPort();
    } catch (URISyntaxException e) {
      throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT", e);
    }
  }

  public Node(byte[] id, String host, int port) {
    this.id = id;
    this.host = host;
    this.port = port;
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

  @Override
  public String toString() {
    return "Node{" +
        " host='" + host + '\'' +
        ", port=" + port +
        ", id=" + ByteArray.toHexString(id) +
        '}';
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

    if (o instanceof Node) {
      return getId().equals(((Node) o).getId());
    }

    return false;
  }
}

/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.protos.Contract.AssetIssueContract;

public class AssetIssueCapsule implements ProtoCapsule<AssetIssueContract> {

  protected static final Logger logger = LoggerFactory.getLogger("AssetIssueCapsule");

  private AssetIssueContract assetIssueContract;

  /**
   * get asset issue contract from bytes data.
   */
  public AssetIssueCapsule(byte[] data) {
    try {
      this.assetIssueContract = AssetIssueContract.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }
  }

  public AssetIssueCapsule(AssetIssueContract assetIssueContract) {
    this.assetIssueContract = assetIssueContract;
  }

  public byte[] getData() {
    return this.assetIssueContract.toByteArray();
  }

  @Override
  public AssetIssueContract getInstance() {
    return this.assetIssueContract;
  }

  @Override
  public String toString() {
    return this.assetIssueContract.toString();
  }

  public ByteString getName() {
    return this.assetIssueContract.getName();
  }
}

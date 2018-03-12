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

package org.tron.core.actuator;

import com.google.common.base.Preconditions;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.core.capsule.AssetIssueCapsule;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract.AssetIssueContract;

public class AssetIssueActuator extends AbstractActuator {

  private static final Logger logger = LoggerFactory.getLogger("AssetIssueActuator");

  AssetIssueActuator(Any contract, Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute() throws ContractExeException {
    try {
      if (!this.contract.is(AssetIssueContract.class)) {
        throw new ContractExeException();
      }

      if (dbManager == null) {
        throw new ContractExeException();
      }

      AssetIssueContract assetIssueContract = contract.unpack(AssetIssueContract.class);

      AssetIssueCapsule assetIssueCapsule = new AssetIssueCapsule(assetIssueContract);

      dbManager.getAssetIssueStore()
          .put(assetIssueCapsule.getName().toByteArray(), assetIssueCapsule);
    } catch (InvalidProtocolBufferException e) {
      throw new ContractExeException();
    }

    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (!this.contract.is(AssetIssueContract.class)) {
      throw new ContractValidateException();
    }

    try {
      final AssetIssueContract assetIssueContract = this.contract.unpack(AssetIssueContract.class);

      Preconditions.checkNotNull(assetIssueContract.getOwnerAddress(), "OwnerAddress is null");
      Preconditions.checkNotNull(assetIssueContract.getName(), "name is null");

      if (this.dbManager.getAssetIssueStore().get(assetIssueContract.getName().toByteArray())
          != null) {
        throw new ContractValidateException();
      }

    } catch (InvalidProtocolBufferException e) {
      throw new ContractValidateException();
    }

    return false;
  }

  @Override
  public ByteString getOwnerAddress() {
    return null;
  }

  @Override
  public long calcFee() {
    return 0;
  }
}

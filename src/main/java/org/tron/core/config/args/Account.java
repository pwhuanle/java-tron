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

package org.tron.core.config.args;

import com.google.protobuf.ByteString;
import com.typesafe.config.ConfigObject;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.StringUtil;
import org.tron.protos.Protocol.AccountType;
import com.google.protobuf.ByteString;

public class Account implements Serializable {

  private static final long serialVersionUID = 2674206490063656846L;

  private static final String ACCOUNT_TYPE_NORMAL = "NORMAL";
  private static final String ACCOUNT_TYPE_ASSETISSUE = "ASSETISSUE";
  private static final String ACCOUNT_TYPE_CONTRACT = "CONTRACT";

  private String accountName;

  private String accountType;

  private String address;

  private String balance;

  public String getAddress() {
    return address;
  }

  public byte[] getAddressBytes() {
    return ByteArray.fromHexString(this.address);
  }

  /**
   * Account address is a 40-bits hex string.
   */
  public void setAddress(String address) {
    if (null == address) {
      throw new IllegalArgumentException(
          "The address(" + address + ") must be a 40-bit hexadecimal string.");
    }

    if (StringUtil.isHexString(address, 40)) {
      this.address = address;
    } else {
      throw new IllegalArgumentException(
          "The address(" + address + ") must be a 40-bit hexadecimal string.");
    }
  }

  public long getBalance() {
    return Long.parseLong(balance);
  }

  /**
   * Account balance is a long type.
   */
  public void setBalance(String balance) {
    try {
      Long.parseLong(balance);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Balance(" + balance + ") must be Long type.");
    }

    this.balance = balance;
  }

  /**
   * get account from configuration.
   */
  public ByteString getAccountName() {
    if (StringUtils.isBlank(this.accountName)) {
      return ByteString.EMPTY;
    }

    return ByteString.copyFrom(ByteArray.fromString(this.accountName));
  }

  /**
   * Account name is a no-empty string.
   */
  public void setAccountName(String accountName) {
    if (StringUtils.isBlank(accountName)) {
      throw new IllegalArgumentException("Account name must be non-empty.");
    }

    this.accountName = accountName;
  }

  /**
   * switch account type.
   */
  public AccountType getAccountType() {
    return getAccountTypeByString(this.accountType);
  }

  /**
   * Account type: Normal/AssetIssue/Contract.
   */
  public void setAccountType(String accountType) {
    if (!this.isAccountType(accountType)) {
      throw new IllegalArgumentException("Account type error: Not Normal/AssetIssue/Contract");
    }

    this.accountType = accountType;
  }

  /**
   * judge account type.
   */
  public boolean isAccountType(String accountType) {
    if (accountType == null) {
      return false;
    }

    switch (accountType.toUpperCase()) {
      case ACCOUNT_TYPE_NORMAL:
      case ACCOUNT_TYPE_ASSETISSUE:
      case ACCOUNT_TYPE_CONTRACT:
        return true;
      default:
        return false;
    }
  }

  /**
   * Normal/AssetIssue/Contract.
   */
  public AccountType getAccountTypeByString(String accountType) {
    if (accountType == null) {
      throw new IllegalArgumentException("Account type error: Not Normal/AssetIssue/Contract");
    }

    switch (accountType.toUpperCase()) {
      case ACCOUNT_TYPE_NORMAL:
        return AccountType.Normal;
      case ACCOUNT_TYPE_ASSETISSUE:
        return AccountType.AssetIssue;
      case ACCOUNT_TYPE_CONTRACT:
        return AccountType.Contract;
      default:
        throw new IllegalArgumentException("Account type error: Not Normal/AssetIssue/Contract");
    }
  }

  public static Account buildFromConfig(final ConfigObject asset) {
    final Account account = new Account();
    account.setAddress(asset.get("address").unwrapped().toString());
    account.setBalance(asset.get("balance").unwrapped().toString());
    return account;
  }
}

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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Account.Vote;
import org.tron.protos.Protocol.AccountType;

public class AccountCapsule implements ProtoCapsule<Account> {

  protected static final Logger logger = LoggerFactory.getLogger("AccountCapsule");

  private byte[] data;

  private Account account;

  private boolean unpacked;

  public AccountCapsule(byte[] data) {
    this.data = data;
    this.unpacked = false;
  }


  private synchronized void unPack() {
    if (unpacked) {
      return;
    }

    try {
      this.account = Account.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }

    unpacked = true;
  }

  /**
   * initial account capsule.
   */
  public AccountCapsule(AccountType accountType, ByteString address, long balance) {
    this.account = Account.newBuilder()
        .setType(accountType)
        .setAddress(address)
        .setBalance(balance)
        .build();
    this.unpacked = true;
  }

  public AccountCapsule(ByteString address, ByteString accountName,
      AccountType accountType, int typeValue) {
    this.account = Account.newBuilder()
        .setType(accountType)
        .setAddress(address)
        .setTypeValue(typeValue)
        .build();
    this.unpacked = true;
  }

  public AccountCapsule(Account account) {
    this.account = account;
    this.unpacked = true;
  }

  public AccountCapsule() {
    this.unpacked = true;
  }

  private void pack() {
    if (this.data == null) {
      this.data = this.account.toByteArray();
    }
  }

  public byte[] getData() {
    pack();
    return data;
  }

  @Override
  public Account getInstance() {
    return this.account;
  }

  public ByteString getAddress() {
    unPack();
    return this.account.getAddress();
  }

  public AccountType getType() {
    unPack();
    return this.account.getType();
  }


  public long getBalance() {
    return this.account.getBalance();
  }

  public void setBalance(long balance) {
    this.account = this.account.toBuilder().setBalance(balance).build();
  }

  @Override
  public String toString() {
    unPack();
    return this.account.toString();
  }


  public void addVotes(ByteString voteAddress, long voteAdd) {
    unPack();
    this.account = this.account.toBuilder()
        .addVotes(Vote.newBuilder().setVoteAddress(voteAddress).setVoteCount(voteAdd).build())
        .build();
  }

  public List<Vote> getVotesList() {
    return this.account.getVotesList();
  }
}

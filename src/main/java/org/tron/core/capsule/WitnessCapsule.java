package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.crypto.ECKey;
import org.tron.protos.Protocol.Witness;

public class WitnessCapsule implements ProtoCapsule<Witness> {

  private static final Logger logger = LoggerFactory.getLogger("WitnessCapsule");

  public static final long MIN_BALANCE = 100;

  private Witness witness;


  /**
   * WitnessCapsule constructor with pubKey and url.
   */
  public WitnessCapsule(final ByteString pubKey, final String url) {
    final Witness.Builder witnessBuilder = Witness.newBuilder();
    this.witness = witnessBuilder
        .setPubKey(pubKey)
        .setAddress(ByteString.copyFrom(ECKey.computeAddress(pubKey.toByteArray())))
        .setUrl(url).build();
  }

  public WitnessCapsule(final Witness witness) {
    this.witness = witness;
  }

  /**
   * WitnessCapsule constructor with address.
   */
  public WitnessCapsule(final ByteString address) {
    this.witness = Witness.newBuilder().setAddress(address).build();
  }

  /**
   * WitnessCapsule constructor with address and voteCount.
   */
  public WitnessCapsule(final ByteString address, final long voteCount, final String url) {
    final Witness.Builder witnessBuilder = Witness.newBuilder();
    this.witness = witnessBuilder
        .setAddress(address)
        .setVoteCount(voteCount).setUrl(url).build();
  }

  public WitnessCapsule(final byte[] data) {
    try {
      this.witness = Witness.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ByteString getAddress() {
    return this.witness.getAddress();
  }

  @Override
  public byte[] getData() {
    return this.witness.toByteArray();
  }

  @Override
  public Witness getInstance() {
    return this.witness;
  }

  public long getLatestBlockNum() {
    return this.witness.getLatestBlockNum();
  }

  public void setPubKey(final ByteString pubKey) {
    this.witness = this.witness.toBuilder().setPubKey(pubKey).build();
  }

  public long getVoteCount() {
    return this.witness.getVoteCount();
  }

  public void setVoteCount(final long voteCount) {
    this.witness = this.witness.toBuilder().setVoteCount(voteCount).build();
  }

  public void setIsJobs(final boolean isJobs) {
    this.witness = this.witness.toBuilder().setIsJobs(isJobs).build();
  }

  public boolean getIsJobs() {
    return this.witness.getIsJobs();
  }
}

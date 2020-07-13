package org.tron.core.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.capsule.TransactionCapsule;

@Slf4j
@Component
public class CrossRevokingStore extends TronStoreWithRevoking<BytesCapsule> {

  public CrossRevokingStore() {
    super("cross-revoke-database");
  }

  public void saveTokenMapping(String chainId, String sourceToken, String descToken) {
    this.put(buildTokenKey(chainId, sourceToken),
        new BytesCapsule(ByteArray.fromString(descToken)));
    this.put(descToken.getBytes(), new BytesCapsule(new byte[1]));
  }

  public boolean containMapping(String token) {
    BytesCapsule data = getUnchecked(token.getBytes());
    if (data != null && !ByteUtil.isNullOrZeroArray(data.getData())) {
      return true;
    } else {
      return false;
    }
  }

  public String getDestTokenFromMapping(String chainId, String sourceToken) {
    BytesCapsule data = getUnchecked(buildTokenKey(chainId, sourceToken));
    if (data != null && !ByteUtil.isNullOrZeroArray(data.getData())) {
      return ByteArray.toStr(data.getData());
    }
    return null;
  }

  public void saveOutTokenCount(String toChainId, String tokenId, long count) {
    this.put(buildOutKey(toChainId, tokenId), new BytesCapsule(ByteArray.fromLong(count)));
  }

  public Long getOutTokenCount(String toChainId, String tokenId) {
    BytesCapsule data = getUnchecked(buildOutKey(toChainId, tokenId));
    if (data != null && !ByteUtil.isNullOrZeroArray(data.getData())) {
      return ByteArray.toLong(data.getData());
    } else {
      return null;
    }
  }

  public void saveInTokenCount(String fromChainId, String tokenId, long count) {
    this.put(buildInKey(fromChainId, tokenId), new BytesCapsule(ByteArray.fromLong(count)));
  }

  public Long getInTokenCount(String fromChainId, String tokenId) {
    BytesCapsule data = getUnchecked(buildInKey(fromChainId, tokenId));
    if (data != null && !ByteUtil.isNullOrZeroArray(data.getData())) {
      return ByteArray.toLong(data.getData());
    } else {
      return null;
    }
  }

  public byte[] getCrossInfoById(String chainId) {
    return getUnchecked(buildRegisterKey(chainId)).getData();
  }

  public void putCrossInfo(String chainId, byte[] crossInfo) {
    byte[] key = buildRegisterKey(chainId);
    this.put(key, new BytesCapsule(crossInfo));
  }

  // 待讨论：只是存数据库，投票期结束后，钱自动返还用户，但是链上只有用户投票的记录，没有钱返还的记录。
  // 会不会发生用户讹我们的情况？
  public void putCrossVote(String chainId, String address, long amount) {
    this.put(buildVoteKey(chainId, address), new BytesCapsule(ByteArray.fromLong(amount)));
  }

  public Long getCrossVote(String chainId, String address) {
    BytesCapsule data = getUnchecked(buildVoteKey(chainId, address));
    if (data != null && !ByteUtil.isNullOrZeroArray(data.getData())) {
      return ByteArray.toLong(data.getData());
    } else {
      return 0L;
    }
  }

  // todo
  public Long getVoteCountByChainId(String chainId) {
    return 0L;
  }

  private byte[] buildTokenKey(String chainId, String tokenId) {
    return ("token_" + chainId + "_" + tokenId).getBytes();
  }

  private byte[] buildOutKey(String toChainId, String tokenId) {
    return ("out_" + toChainId + "_" + tokenId).getBytes();
  }

  private byte[] buildInKey(String fromChainId, String tokenId) {
    return ("in_" + fromChainId + "_" + tokenId).getBytes();
  }

  private byte[] buildRegisterKey(String chainId) {
    return ("register_" + chainId).getBytes();
  }

  private byte[] buildVoteKey(String chainId, String address) {
    return ("vote_" + chainId + address).getBytes();
  }

}

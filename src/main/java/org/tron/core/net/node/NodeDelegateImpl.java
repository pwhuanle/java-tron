package org.tron.core.net.node;

import com.google.common.primitives.Longs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.overlay.message.Message;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.BlockCapsule.BlockId;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.Parameter.NodeConstant;
import org.tron.core.db.Manager;
import org.tron.core.exception.BadBlockException;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.BadTransactionException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.DupTransactionException;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.core.exception.StoreException;
import org.tron.core.exception.TaposException;
import org.tron.core.exception.TronException;
import org.tron.core.exception.UnLinkedBlockException;
import org.tron.core.exception.ValidateBandwidthException;
import org.tron.core.exception.ValidateScheduleException;
import org.tron.core.exception.ValidateSignatureException;
import org.tron.core.net.message.BlockMessage;
import org.tron.core.net.message.MessageTypes;
import org.tron.core.net.message.TransactionMessage;

@Slf4j
public class NodeDelegateImpl implements NodeDelegate {

  private Manager dbManager;

  public NodeDelegateImpl(Manager dbManager) {
    this.dbManager = dbManager;
  }

  @Override
  public synchronized LinkedList<Sha256Hash> handleBlock(BlockCapsule block, boolean syncMode)
      throws BadBlockException, UnLinkedBlockException {
    // TODO timestamp shouble be consistent.
    long gap = System.currentTimeMillis() - block.getTimeStamp();
    if (gap / 1000 < -6000) {
      throw new BadBlockException("block time error");
    }
    try {
      dbManager.pushBlock(block);
      if (!syncMode) {
        List<TransactionCapsule> trx = null;
        trx = block.getTransactions();
        return trx.stream()
            .map(TransactionCapsule::getHash)
            .collect(Collectors.toCollection(LinkedList::new));
      } else {
        return null;
      }

    } catch (ValidateScheduleException e) {
      throw new BadBlockException("validate schedule exception");
    } catch (ValidateSignatureException e) {
      throw new BadBlockException("validate signature exception");
    } catch (ContractValidateException e) {
      throw new BadBlockException("ContractValidate exception");
    } catch (ContractExeException e) {
      throw new BadBlockException("Contract Exectute exception");
    }
  }


  @Override
  public void handleTransaction(TransactionCapsule trx) throws BadTransactionException {
    logger.info("handle transaction");
    try {
      dbManager.pushTransactions(trx);
    } catch (ContractValidateException e) {
      logger.error("Contract validate failed", e);
      throw new BadTransactionException();
    } catch (ContractExeException e) {
      logger.error("Contract execute failed", e);
      throw new BadTransactionException();
    } catch (ValidateSignatureException e) {
      logger.error("ValidateSignatureException");
      throw new BadTransactionException();
    } catch (ValidateBandwidthException e) {
      logger.error("ValidateBandwidthException");
    } catch (DupTransactionException e) {
      logger.error("dup trans");
    } catch (TaposException e) {
      logger.error("tapos error");
    }
  }

  @Override
  public LinkedList<BlockId> getLostBlockIds(List<BlockId> blockChainSummary)
      throws StoreException {
    //todo: return the remain block count.
    //todo: return the blocks it should be have.
    if (dbManager.getHeadBlockNum() == 0) {
      return new LinkedList<>();
    }

    BlockId unForkedBlockId;

    if (blockChainSummary.isEmpty() ||
        (blockChainSummary.size() == 1
            && blockChainSummary.get(0).equals(dbManager.getGenesisBlockId()))) {
      unForkedBlockId = dbManager.getGenesisBlockId();
    } else if (blockChainSummary.size() == 1
        && blockChainSummary.get(0).getNum() == 0) {
      return new LinkedList<BlockId>() {{
        add(dbManager.getGenesisBlockId());
      }};
    } else {
      //todo: find a block we all know between the summary and my db.
      Collections.reverse(blockChainSummary);
      unForkedBlockId = blockChainSummary.stream()
          .filter(blockId -> containBlockInMainChain(blockId))
          .findFirst().orElse(null);
      if (unForkedBlockId == null) {
        return new LinkedList<>();
      }
      //todo: can not find any same block form peer's summary and my db.
    }

    //todo: limit the count of block to send peer by one time.
    long unForkedBlockIdNum = unForkedBlockId.getNum();
    long len = Longs
        .min(dbManager.getHeadBlockNum(), unForkedBlockIdNum + NodeConstant.SYNC_FETCH_BATCH_NUM);

    LinkedList<BlockId> blockIds = new LinkedList<>();
    for (long i = unForkedBlockIdNum; i <= len; i++) {
      BlockId id = dbManager.getBlockIdByNum(i);
      blockIds.add(id);
    }
    return blockIds;
  }

  @Override
  public Deque<BlockId> getBlockChainSummary(BlockId beginBLockId, Deque<BlockId> blockIdsToFetch)
      throws TronException {

    Deque<BlockId> retSummary = new LinkedList<>();
    List<BlockId> blockIds = new ArrayList<>(blockIdsToFetch);
    long highBlkNum;
    long highNoForkBlkNum;
    long lowBlkNum = dbManager.getSyncBeginNumber() < 0 ? 0 : dbManager.getSyncBeginNumber();

    LinkedList<BlockId> forkList = new LinkedList<>();

    if (!beginBLockId.equals(getGenesisBlock().getBlockId())) {
      if (containBlockInMainChain(beginBLockId)) {
        highBlkNum = beginBLockId.getNum();
        if (highBlkNum == 0) {
          throw new TronException(
              "This block don't equal my genesis block hash, but it is in my DB, the block id is :"
                  + beginBLockId.getString());
        }
        highNoForkBlkNum = highBlkNum;
        if (beginBLockId.getNum() < lowBlkNum) {
          lowBlkNum = beginBLockId.getNum();
        }
      } else {
        forkList = dbManager.getBlockChainHashesOnFork(beginBLockId);
        if (forkList.isEmpty()) {
          throw new UnLinkedBlockException(
              "We want to find forkList of this block: " + beginBLockId.getString()
                  + " ,but in KhasoDB we can not find it, It maybe a very old beginBlockId, we are sync once,"
                  + " we swift and pop it after that time. ");
        }
        highNoForkBlkNum = forkList.peekLast().getNum();
        forkList.pollLast();
        Collections.reverse(forkList);
        highBlkNum = highNoForkBlkNum + forkList.size();
        if (highNoForkBlkNum < lowBlkNum) {
          throw new UnLinkedBlockException(
              "It is a too old block that we take it as a forked block long long ago"
                  + "\n lowBlkNum:" + lowBlkNum
                  + "\n highNoForkBlkNum" + highNoForkBlkNum);
        }
      }
    } else {
      highBlkNum = dbManager.getHeadBlockNum();
      highNoForkBlkNum = highBlkNum;
    }

    if (!blockIds.isEmpty() && highBlkNum != blockIds.get(0).getNum() - 1) {
      logger.error("Check ERROR: highBlkNum:" + highBlkNum + ",blockIdToSyncFirstNum is "
          + blockIds.get(0).getNum() + ",blockIdToSyncEnd is " + blockIds.get(blockIds.size() - 1)
          .getNum());
    }

    long realHighBlkNum = highBlkNum + blockIds.size();
    do {
      if (lowBlkNum <= highNoForkBlkNum) {
        retSummary.offer(dbManager.getBlockIdByNum(lowBlkNum));
      } else if (lowBlkNum <= highBlkNum) {
        retSummary.offer(forkList.get((int) (lowBlkNum - highNoForkBlkNum - 1)));
      } else {
        retSummary.offer(blockIds.get((int) (lowBlkNum - highBlkNum - 1)));
      }
      lowBlkNum += (realHighBlkNum - lowBlkNum + 2) / 2;
    } while (lowBlkNum <= realHighBlkNum);

    return retSummary;
  }

  @Override
  public Message getData(Sha256Hash hash, MessageTypes type) {
    switch (type) {
      case BLOCK:
        try {
          return new BlockMessage(dbManager.getBlockById(hash));
        } catch (BadItemException e) {
          logger.debug(e.getMessage());
        } catch (ItemNotFoundException e) {
          logger.debug(e.getMessage());
        }
      case TRX:
        return new TransactionMessage(
            dbManager.getTransactionStore().get(hash.getBytes()).getData());
      default:
        logger.info("message type not block or trx.");
        return null;
    }
  }

  @Override
  public void syncToCli(long unSyncNum) {
    logger.info("There are " + unSyncNum + " blocks we need to sync.");
    if (unSyncNum == 0) {
      logger.info("Sync Block Completed !!!");
    }
    dbManager.setSyncMode(unSyncNum == 0);
    //TODO: notify cli know how many block we need to sync
  }

  @Override
  public long getBlockTime(BlockId id) {
    try {
      return dbManager.getBlockById(id).getTimeStamp();
    } catch (BadItemException e) {
      return dbManager.getGenesisBlock().getTimeStamp();
    } catch (ItemNotFoundException e) {
      return dbManager.getGenesisBlock().getTimeStamp();
    }
  }

  @Override
  public BlockId getHeadBlockId() {
    return dbManager.getHeadBlockId();
  }

  @Override
  public long getHeadBlockTimeStamp() {
    return dbManager.getHeadBlockTimeStamp();
  }

  @Override
  public boolean containBlock(BlockId id) {
    return dbManager.containBlock(id);
  }

  @Override
  public boolean containBlockInMainChain(BlockId id) {
    return dbManager.containBlockInMainChain(id);
  }

  @Override
  public boolean contain(Sha256Hash hash, MessageTypes type) {
    if (type.equals(MessageTypes.BLOCK)) {
      return dbManager.containBlock(hash);
    } else if (type.equals(MessageTypes.TRX)) {
      //TODO: check it
      return dbManager.getTransactionStore().has(hash.getBytes());
    }
    return false;
  }

  @Override
  public BlockCapsule getGenesisBlock() {
    //TODO return a genesisBlock
    return dbManager.getGenesisBlock();
  }

  //  @Override
//  public long getLatestSolidifiedBlockNum() {
//    return dbManager.getDynamicPropertiesStore().getLatestSolidifiedBlockNum();
//  }
//
//  @Override
//  public long getSyncBeginNumber() {
//    return dbManager.getSyncBeginNumber();
//  }
//
  @Override
  public boolean canChainRevoke(long num) {
    return num >= dbManager.getSyncBeginNumber();
  }
}

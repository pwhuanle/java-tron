package org.tron.core.db;

import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.config.args.Args;

@Slf4j
public class DynamicPropertiesStore extends TronStoreWithRevoking<BytesCapsule> {

  private static final long MAINTENANCE_TIME_INTERVAL = 24 * 3600 * 1000;// (ms)
  private static final long MAINTENANCE_SKIP_SLOTS = 2;

  private static final byte[] LATEST_BLOCK_HEADER_TIMESTAMP = "latest_block_header_timestamp"
      .getBytes();
  private static final byte[] LATEST_BLOCK_HEADER_NUMBER = "latest_block_header_number".getBytes();
  private static final byte[] LATEST_BLOCK_HEADER_HASH = "latest_block_header_hash".getBytes();
  private static final byte[] STATE_FLAG = "state_flag"
      .getBytes(); // 1 : is maintenance, 0 : is not maintenance
  private static final byte[] LATEST_SOLIDIFIED_BLOCK_NUM = "LATEST_SOLIDIFIED_BLOCK_NUM"
      .getBytes();

  private static final byte[] BLOCK_FILLED_SLOTS = "BLOCK_FILLED_SLOTS".getBytes();

  private static final int BLOCK_FILLED_SLOTS_NUMBER = 128;

  private int blockFilledSlotsIndex = 0;

  private DateTime nextMaintenanceTime = new DateTime(
      Long.parseLong(Args.getInstance().getGenesisBlock().getTimestamp()));

  private DynamicPropertiesStore(String dbName) {
    super(dbName);
    try {
      this.getLatestBlockHeaderTimestamp();
    } catch (IllegalArgumentException e) {
      this.saveLatestBlockHeaderTimestamp(0);
    }

    try {
      this.getLatestBlockHeaderNumber();
    } catch (IllegalArgumentException e) {
      this.saveLatestBlockHeaderNumber(0);
    }

    try {
      this.getLatestBlockHeaderHash();
    } catch (IllegalArgumentException e) {
      this.saveLatestBlockHeaderHash(ByteString.copyFrom(ByteArray.fromHexString("00")));
    }

    try {
      this.getStateFlag();
    } catch (IllegalArgumentException e) {
      this.saveStateFlag(0);
    }

    try {
      this.getLatestSolidifiedBlockNum();
    } catch (IllegalArgumentException e) {
      this.saveLatestSolidifiedBlockNum(0);
    }

    try {
      this.getBlockFilledSlots();
    } catch (IllegalArgumentException e) {
      int[] blockFilledSlots = new int[BLOCK_FILLED_SLOTS_NUMBER];
      Arrays.fill(blockFilledSlots, 1);
      this.saveBlockFilledSlots(blockFilledSlots);
    }
  }


  @Override
  public void delete(byte[] key) {

  }

  @Override
  public BytesCapsule get(byte[] key) {
    return null;
  }

  @Override
  public boolean has(byte[] key) {
    return false;
  }

  private static DynamicPropertiesStore instance;

  public void destroy() {
    instance = null;
  }

  /**
   * create fun.
   *
   * @param dbName the name of database
   */

  public static DynamicPropertiesStore create(String dbName) {
    if (instance == null) {
      synchronized (DynamicPropertiesStore.class) {
        if (instance == null) {
          instance = new DynamicPropertiesStore(dbName);
        }
      }
    }
    return instance;
  }

  public String intArrayToString(int[] a) {
    StringBuilder sb = new StringBuilder();
    for (int i : a) {
      sb.append(i);
    }
    return sb.toString();
  }

  public int[] stringToIntArray(String s) {
    int length = s.length();
    int[] result = new int[length];
    for (int i = 0; i < length; ++i) {
      result[i] = Integer.parseInt(s.substring(i, i + 1));
    }
    return result;
  }

  public void saveBlockFilledSlots(int[] blockFilledSlots) {
    logger.debug("blockFilledSlots:" + intArrayToString(blockFilledSlots));
    this.put(BLOCK_FILLED_SLOTS,
        new BytesCapsule(ByteArray.fromString(intArrayToString(blockFilledSlots))));
  }

  public int[] getBlockFilledSlots() {
    return Optional.ofNullable(this.dbSource.getData(BLOCK_FILLED_SLOTS))
        .map(ByteArray::toStr)
        .map(this::stringToIntArray)
        .orElseThrow(
            () -> new IllegalArgumentException("not found latest SOLIDIFIED_BLOCK_NUM timestamp"));
    //return ByteArray.toLong(this.dbSource.getData(this.SOLIDIFIED_THRESHOLD));
  }

  public void applyBlock(boolean fillBlock) {
    int[] blockFilledSlots = getBlockFilledSlots();
    blockFilledSlots[blockFilledSlotsIndex] = fillBlock ? 1 : 0;
    blockFilledSlotsIndex = (blockFilledSlotsIndex + 1) % BLOCK_FILLED_SLOTS_NUMBER;
    saveBlockFilledSlots(blockFilledSlots);
  }

  public int calculateFilledSlotsCount() {
    int[] blockFilledSlots = getBlockFilledSlots();
    return 100 * IntStream.of(blockFilledSlots).sum() / BLOCK_FILLED_SLOTS_NUMBER;
  }

  public void saveLatestSolidifiedBlockNum(long number) {
    this.put(LATEST_SOLIDIFIED_BLOCK_NUM, new BytesCapsule(ByteArray.fromLong(number)));
  }

  public long getLatestSolidifiedBlockNum() {
    return Optional.ofNullable(this.dbSource.getData(LATEST_SOLIDIFIED_BLOCK_NUM))
        .map(ByteArray::toLong)
        .orElseThrow(
            () -> new IllegalArgumentException("not found latest SOLIDIFIED_BLOCK_NUM timestamp"));
    //return ByteArray.toLong(this.dbSource.getData(this.SOLIDIFIED_THRESHOLD));
  }

  /**
   * get timestamp of creating global latest block.
   */
  public long getLatestBlockHeaderTimestamp() {
    return Optional.ofNullable(this.dbSource.getData(LATEST_BLOCK_HEADER_TIMESTAMP))
        .map(ByteArray::toLong)
        .orElseThrow(() -> new IllegalArgumentException("not found latest block header timestamp"));
  }

  /**
   * get number of global latest block.
   */
  public long getLatestBlockHeaderNumber() {
    return Optional.ofNullable(this.dbSource.getData(LATEST_BLOCK_HEADER_NUMBER))
        .map(ByteArray::toLong)
        .orElseThrow(() -> new IllegalArgumentException("not found latest block header number"));
  }

  public int getStateFlag() {
    return Optional.ofNullable(this.dbSource.getData(STATE_FLAG))
        .map(ByteArray::toInt)
        .orElseThrow(() -> new IllegalArgumentException("not found maintenance flag"));
  }

  /**
   * get id of global latest block.
   */

  public Sha256Hash getLatestBlockHeaderHash() {

    byte[] blockHash = Optional.ofNullable(this.dbSource.getData(LATEST_BLOCK_HEADER_HASH))
        .orElseThrow(() -> new IllegalArgumentException("not found block hash"));
    return Sha256Hash.wrap(blockHash);
  }

  /**
   * save timestamp of creating global latest block.
   */
  public void saveLatestBlockHeaderTimestamp(long t) {
    logger.info("update latest block header timestamp = {}", t);
    this.put(LATEST_BLOCK_HEADER_TIMESTAMP, new BytesCapsule(ByteArray.fromLong(t)));
  }

  /**
   * save number of global latest block.
   */
  public void saveLatestBlockHeaderNumber(long n) {
    logger.info("update latest block header number = {}", n);
    this.put(LATEST_BLOCK_HEADER_NUMBER, new BytesCapsule(ByteArray.fromLong(n)));
  }

  /**
   * save id of global latest block.
   */
  public void saveLatestBlockHeaderHash(ByteString h) {
    logger.info("update latest block header id = {}", ByteArray.toHexString(h.toByteArray()));
    this.put(LATEST_BLOCK_HEADER_HASH, new BytesCapsule(h.toByteArray()));
  }

  public void saveStateFlag(int n) {
    logger.info("update state flag = {}", n);
    this.put(STATE_FLAG, new BytesCapsule(ByteArray.fromInt(n)));
  }


  public DateTime getNextMaintenanceTime() {
    return nextMaintenanceTime;
  }

  public long getMaintenanceSkipSlots() {
    return MAINTENANCE_SKIP_SLOTS;
  }

  private void setNextMaintenanceTime(DateTime nextMaintenanceTime) {
    this.nextMaintenanceTime = nextMaintenanceTime;
  }

  public void updateNextMaintenanceTime(long blockTime) {

    long maintenanceTimeInterval = MAINTENANCE_TIME_INTERVAL;
    DateTime currentMaintenanceTime = getNextMaintenanceTime();
    long round = (blockTime - currentMaintenanceTime.getMillis()) / maintenanceTimeInterval;
    DateTime nextMaintenanceTime = currentMaintenanceTime
        .plus((round + 1) * maintenanceTimeInterval);
    setNextMaintenanceTime(nextMaintenanceTime);

    logger.info(
        "do update nextMaintenanceTime,currentMaintenanceTime:{}, blockTime:{},nextMaintenanceTime:{}",
        new DateTime(currentMaintenanceTime), new DateTime(blockTime),
        new DateTime(nextMaintenanceTime)
    );
  }

}

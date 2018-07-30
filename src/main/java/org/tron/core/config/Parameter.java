package org.tron.core.config;

public interface Parameter {

  interface ChainConstant {

    long TRANSFER_FEE = 0; // free
    int WITNESS_STANDBY_LENGTH = 127;
    int SOLIDIFIED_THRESHOLD = 70; // 70%
    int PRIVATE_KEY_LENGTH = 64;
    int MAX_ACTIVE_WITNESS_NUM = 27;
    int BLOCK_SIZE = 2_000_000;
    int BLOCK_PRODUCED_INTERVAL = 3000; //ms,produce block period, must be divisible by 60. millisecond
    long CLOCK_MAX_DELAY = 3600000; // 3600 * 1000 ms
    int BLOCK_PRODUCED_TIME_OUT = 75; // 75%
    long PRECISION = 1000_000;
    long WINDOW_SIZE_MS = 24 * 3600 * 1000L;
    long MS_PER_DAY = 24 * 3600 * 1000L;
    long MS_PER_YEAR = 365 * 24 * 3600 * 1000L;

    long MAINTENANCE_SKIP_SLOTS = 2;
    int SINGLE_REPEAT = 1;
    int BLOCK_FILLED_SLOTS_NUMBER = 128;
    int MAX_VOTE_NUMBER = 30;
    int MAX_FROZEN_NUMBER = 1;

  }

  interface NodeConstant {

    long SYNC_RETURN_BATCH_NUM = 1000;
    long SYNC_FETCH_BATCH_NUM = 2000;
    long MAX_BLOCKS_IN_PROCESS = 400;
    long MAX_BLOCKS_ALREADY_FETCHED = 800;
    long MAX_BLOCKS_SYNC_FROM_ONE_PEER = 1000;
    long SYNC_CHAIN_LIMIT_NUM = 500;
    int MAX_TRANSACTION_PENDING = 2000;
  }

  interface NetConstants {

    long GRPC_IDLE_TIME_OUT = 60000L;
    long ADV_TIME_OUT = 20000L;
    long SYNC_TIME_OUT = 5000L;
    long HEAD_NUM_MAX_DELTA = 1000L;
    long HEAD_NUM_CHECK_TIME = 60000L;
    int MAX_INVENTORY_SIZE_IN_MINUTES = 2;
    long NET_MAX_TRX_PER_SECOND = 700L;
    long MAX_TRX_PER_PEER = 200L;
    int NET_MAX_INV_SIZE_IN_MINUTES = 2;
    int MSG_CACHE_DURATION_IN_BLOCKS = 5;
  }

  interface DatabaseConstants {
    int TRANSACTIONS_COUNT_LIMIT_MAX = 1000;
    int ASSET_ISSUE_COUNT_LIMIT_MAX = 1000;
  }

  enum ChainParameters {
    MAINTENANCE_TIME_INTERVAL, //ms
    ACCOUNT_UPGRADE_COST, //drop
    CREATE_ACCOUNT_FEE, //drop
    TRANSACTION_FEE, //drop
    ASSET_ISSUE_FEE, //drop
    WITNESS_PAY_PER_BLOCK, //drop
    WITNESS_STANDBY_ALLOWANCE, //drop
    CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT, //drop
    CREATE_NEW_ACCOUNT_BANDWIDTH_RATE, // 1 ~
//    ONE_DAY_NET_LIMIT,
//    MAX_FROZEN_TIME,
//    MIN_FROZEN_TIME,
//    MAX_FROZEN_SUPPLY_NUMBER,
//    MAX_FROZEN_SUPPLY_TIME,
//    MIN_FROZEN_SUPPLY_TIME,
//    WITNESS_ALLOWANCE_FROZEN_TIME,
//    PUBLIC_NET_LIMIT,
//    FREE_NET_LIMIT,
//    TOTAL_NET_LIMIT
  }

}

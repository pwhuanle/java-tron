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

package org.tron.core.manager;

import java.io.FileInputStream;
import java.util.Scanner;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.common.utils.ExecutorPipeline;
import org.tron.core.TransactionUtils;
import org.tron.core.TronBlockChainImpl;
import org.tron.core.config.SystemProperties;
import org.tron.protos.core.TronBlock;
import org.tron.protos.core.TronTransaction;

public class TronBlockLoader {

  private static final Logger logger = LoggerFactory.getLogger("TronBlockLoader");
  @Autowired
  SystemProperties config;
  Scanner scanner = null;
  ExecutorPipeline<TronBlock.Block, TronBlock.Block> exce1;
  ExecutorPipeline<TronBlock.Block, ?> exce2;
  @Autowired
  private TronBlockChainImpl blockchain;

  public void loadBlocks() {

    exce1 = new ExecutorPipeline(8, 1000, true,
        (Function<TronBlock.Block, TronBlock.Block>) block -> {
          if (block.getBlockHeader().getNumber() >= blockchain
              .getBlockStoreInter().getBestBlock().getBlockHeader()
              .getNumber()) {
            for (TronTransaction.Transaction tx : block
                .getTransactionsList()) {
              TransactionUtils.getSender(tx);

            }
          }
          return block;
        }, throwable -> logger.error("Unhandled exception: ", throwable));

    exce2 = exce1.add(1, 1000, block -> {
      try {
        blockWork(block);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    String fileSrc = config.blocksLoader();

    try {
      final String blocksFormat = config.getConfig().hasPath("blocks" +
          ".format") ? config.getConfig().getString
          ("blocks.format") : null;
      System.out.println("Loading blocks: " + fileSrc + ", format: " +
          blocksFormat);

      FileInputStream inputStream = new FileInputStream(fileSrc);

      scanner = new Scanner(inputStream, "UTF-8");

      while (scanner.hasNext()) {
        byte[] blockBytes = Hex.decode(scanner.nextLine());
        TronBlock.Block block = TronBlock.Block.parseFrom(blockBytes);

        exce1.push(block);
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

  private void blockWork(TronBlock.Block block) {
    if (block.getBlockHeader().getNumber() >= blockchain.getBlockStoreInter()
        .getBestBlock().getBlockHeader().getNumber()
        || blockchain.getBlockStoreInter().getBlockByHash(block
        .getBlockHeader().getHash().toByteArray()) == null) {
      if (block.getBlockHeader().getNumber() > 0) {
        throw new RuntimeException();
      }
    }
  }
}

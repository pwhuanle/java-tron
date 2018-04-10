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

package org.tron.core;

import org.tron.common.utils.ByteArray;

public class Constant {

  // whole
  public static final byte[] LAST_HASH = ByteArray.fromString("lastHash");
  public static final String DIFFICULTY = "2001";

  // DB
  public static final String BLOCK_DB_NAME = "block_data";
  public static final String TRANSACTION_DB_NAME = "transaction_data";

  //config
  public static final String NORMAL = "normal";
  public static final String TEST = "test";
  public static final String NORMAL_CONF = "config.conf";
  public static final String TEST_CONF = "config-test.conf";
  public static final String DATABASE_DIR = "storage.directory";

  public static final byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0xa1;   //a1 + address  ,a1 is version
  public static final String ADD_PRE_FIX_STRING_MAINNET = "a1";
  public static final byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address  ,a0 is version
  public static final String ADD_PRE_FIX_STRING_TESTNET = "a0";
  public static final int ADDRESS_SIZE = 42;
}

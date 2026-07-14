/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.connection.pipeline;

import io.netty.util.ByteProcessor;

public class VarIntByteDecoder implements ByteProcessor {

  private int readVarInt;
  private int bytesRead;
  private DecodeResult result = DecodeResult.TOO_SHORT;

  @Override
  public boolean process(byte k) {
    readVarInt |= (k & 0x7F) << bytesRead++ * 7;
    if (bytesRead > 3) {
      result = DecodeResult.TOO_BIG;
      return false;
    }
    if ((k & 0x80) != 128) {
      result = DecodeResult.SUCCESS;
      return false;
    }
    return true;
  }

  public int getReadVarInt() {
    return readVarInt;
  }

  public int getBytesRead() {
    return bytesRead;
  }

  public DecodeResult getResult() {
    return result;
  }

  public enum DecodeResult {
    SUCCESS,
    TOO_SHORT,
    TOO_BIG
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ByteProcessor;
import java.util.List;

/**
 * Splits the inbound stream into uncompressed varint-length-prefixed Minecraft packets.
 *
 * <p>Skips leading {@code 0x00} runs some proxies emit, reads a length varint of at most 5 bytes,
 * and rejects garbage hard: a varint that still continues after 5 bytes, a length that decodes to
 * {@code <= 0}, and any frame above {@link #MAX_FRAME_BYTES} all raise a {@link DecoderException}
 * and drop the connection. Because the largest legal frame (2097151 bytes, 2 MiB - 1) is exactly
 * the biggest value a 3-byte varint can hold, the size cap supersedes the old 21-bit "VarInt too
 * big" check while accepting the same traffic. Incomplete frames wait for more bytes; there is no
 * compression anywhere.
 */
public final class VarIntFrameDecoder extends ByteToMessageDecoder {

  /** Largest accepted frame: 2 MiB - 1, the maximum length a 3-byte varint can encode. */
  public static final int MAX_FRAME_BYTES = 2097151;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    if (!ctx.channel().isActive()) {
      in.clear();
      return;
    }

    int packetStart = in.forEachByte(ByteProcessor.FIND_NON_NUL);
    if (packetStart == -1) {
      in.clear();
      return;
    }
    in.readerIndex(packetStart);

    in.markReaderIndex();
    int length = readVarInt(in);
    if (length < 0) {
      in.resetReaderIndex();
      return;
    }
    if (length <= 0) {
      throw new DecoderException("Bad VarInt length: " + length);
    }
    if (length > MAX_FRAME_BYTES) {
      throw new DecoderException("Frame too big: " + length + " bytes");
    }
    if (in.readableBytes() < length) {
      in.resetReaderIndex();
      return;
    }

    out.add(in.readRetainedSlice(length));
  }

  /**
   * Reads a Minecraft varint of at most 5 bytes, {@code -1} when the varint is not fully buffered
   * yet, or throws when it does not fit a positive int. The accumulator is a long so a
   * completed-but-overflowing varint can never alias the {@code -1} incomplete sentinel.
   */
  private static int readVarInt(ByteBuf in) {
    long result = 0;
    for (int i = 0; i < 5; i++) {
      if (!in.isReadable()) {
        return -1;
      }
      byte b = in.readByte();
      result |= (long) (b & 0x7F) << (i * 7);
      if ((b & 0x80) == 0) {
        if (result > Integer.MAX_VALUE) {
          throw new DecoderException("VarInt too big");
        }
        return (int) result;
      }
    }
    throw new DecoderException("VarInt too big");
  }
}

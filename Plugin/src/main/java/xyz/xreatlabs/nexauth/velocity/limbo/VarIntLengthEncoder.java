/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Prefixes outbound packets with their uncompressed varint length. The opposite of {@link
 * VarIntFrameDecoder}; there is no compression anywhere in the limbo pipeline.
 */
@ChannelHandler.Sharable
public final class VarIntLengthEncoder extends MessageToByteEncoder<ByteBuf> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
    writeVarInt(out, msg.readableBytes());
    out.writeBytes(msg);
  }

  @Override
  protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
    return ctx.alloc().heapBuffer(5 + msg.readableBytes());
  }

  /** Writes a Minecraft varint (1-5 bytes, little-endian 7-bit groups). */
  public static void writeVarInt(ByteBuf out, int value) {
    do {
      int group = value & 0x7F;
      value >>>= 7;
      if (value != 0) {
        group |= 0x80;
      }
      out.writeByte(group);
    } while (value != 0);
  }
}

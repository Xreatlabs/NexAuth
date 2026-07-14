/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.connection.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ua.nanit.limbo.protocol.ByteMessage;

@ChannelHandler.Sharable
public class VarIntLengthEncoder extends MessageToByteEncoder<ByteBuf> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf out) {
    ByteMessage msg = new ByteMessage(out);
    msg.writeVarInt(buf.readableBytes());
    msg.writeBytes(buf);
  }

  @Override
  protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
    int anticipatedRequiredCapacity = 5 + msg.readableBytes();
    return ctx.alloc().heapBuffer(anticipatedRequiredCapacity);
  }
}

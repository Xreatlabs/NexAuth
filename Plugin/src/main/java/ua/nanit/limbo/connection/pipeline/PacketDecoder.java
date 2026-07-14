/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.connection.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

  private State.PacketRegistry mappings;
  private Version version;

  public PacketDecoder() {
    updateVersion(Version.getMin());
    updateState(State.HANDSHAKING);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
    if (!ctx.channel().isActive() || mappings == null) return;

    ByteMessage msg = new ByteMessage(buf);
    int packetId = msg.readVarInt();
    Packet packet = mappings.getPacket(packetId);
    if (packet == null) {
      Log.debug("Undefined incoming packet: 0x" + Integer.toHexString(packetId));
      return;
    }

    Log.debug(
        "Received packet %s[0x%s] (%d bytes)",
        packet.toString(), Integer.toHexString(packetId), msg.readableBytes());

    try {
      packet.decode(msg, version);
    } catch (Exception e) {
      throw new DecoderException("Cannot decode packet 0x" + Integer.toHexString(packetId), e);
    }

    if (buf.isReadable()) {
      throw new DecoderException(
          "Packet 0x"
              + Integer.toHexString(packetId)
              + " larger than expected, extra bytes: "
              + msg.readableBytes());
    }

    ctx.fireChannelRead(packet);
  }

  public void updateVersion(Version version) {
    this.version = version;
  }

  public void updateState(State state) {
    this.mappings = state.serverBound.getRegistry(version);
  }
}

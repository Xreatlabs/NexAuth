/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.connection.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

  private State.PacketRegistry registry;
  private Version version;

  public PacketEncoder() {
    updateVersion(Version.getMin());
    updateState(State.HANDSHAKING);
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
    if (registry == null) return;

    ByteMessage msg = new ByteMessage(out);
    int packetId;

    if (packet instanceof PacketSnapshot) {
      packetId = registry.getPacketId(((PacketSnapshot) packet).getPacketClass());
    } else {
      packetId = registry.getPacketId(packet.getClass());
    }

    if (packetId == -1) {
      Log.warning(
          "Undefined packet class: %s[0x%s] (%d bytes)",
          packet.getClass().getName(), Integer.toHexString(packetId), msg.readableBytes());
      return;
    }

    msg.writeVarInt(packetId);

    try {
      packet.encode(msg, version);

      if (Log.isDebug()) {
        Log.debug(
            "Sending %s[0x%s] packet (%d bytes)",
            packet.toString(), Integer.toHexString(packetId), msg.readableBytes());
      }
    } catch (Exception e) {
      Log.error("Cannot encode packet 0x%s: %s", Integer.toHexString(packetId), e.getMessage());
    }
  }

  public void updateVersion(Version version) {
    this.version = version;
  }

  public void updateState(State state) {
    this.registry = state.clientBound.getRegistry(version);
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import io.netty.handler.codec.DecoderException;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public class PacketLoginPluginResponse implements PacketIn {

  private int messageId;
  private boolean successful;
  private ByteMessage data;

  public int getMessageId() {
    return messageId;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public ByteMessage getData() {
    return data;
  }

  @Override
  public void decode(ByteMessage msg, Version version) {
    messageId = msg.readVarInt();
    successful = msg.readBoolean();

    if (msg.readableBytes() > 0) {
      int readableBytes = msg.readableBytes();
      if (readableBytes > Short.MAX_VALUE) {
        throw new DecoderException("Cannot receive payload larger than " + Short.MAX_VALUE);
      }
      data = new ByteMessage(msg.readBytes(readableBytes));
    }
  }

  @Override
  public void handle(ClientConnection conn, LimboServer server) {
    server.getPacketHandler().handle(conn, this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketLoginPluginRequest implements PacketOut {

  private int messageId;
  private String channel;
  private ByteBuf data;

  public void setMessageId(int messageId) {
    this.messageId = messageId;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public void setData(ByteBuf data) {
    this.data = data;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeVarInt(messageId);
    msg.writeString(channel);
    msg.writeBytes(data);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

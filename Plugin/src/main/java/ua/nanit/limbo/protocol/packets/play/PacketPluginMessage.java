/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import io.netty.handler.codec.DecoderException;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketPluginMessage implements PacketIn, PacketOut {

  private String channel;
  private byte[] data;

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeString(channel);
    msg.writeBytes(data);
  }

  @Override
  public void decode(ByteMessage msg, Version version) {
    channel = msg.readString();
    int readableBytes = msg.readableBytes();
    if (readableBytes > Short.MAX_VALUE) {
      throw new DecoderException("Cannot receive payload larger than " + Short.MAX_VALUE);
    }
    data = new byte[msg.readableBytes()];
    msg.readBytes(data);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

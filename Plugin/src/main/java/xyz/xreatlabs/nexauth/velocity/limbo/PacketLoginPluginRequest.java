/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Login plugin request for modern forwarding ({@code velocity:player_info}, zero data). */
public final class PacketLoginPluginRequest {

  public static final String VELOCITY_CHANNEL = "velocity:player_info";

  private final int messageId;

  public PacketLoginPluginRequest(int messageId) {
    this.messageId = messageId;
  }

  public void write(ByteBuf buf, int protocol) {
    Buf.writeVarInt(buf, messageId);
    Buf.writeString(buf, VELOCITY_CHANNEL);
  }
}

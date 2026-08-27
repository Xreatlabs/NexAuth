/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

/** Login success: always the constant "NexAuth" profile, never the forwarded player. */
public final class PacketLoginSuccess {

  private final UUID uuid;
  private final String username;

  public PacketLoginSuccess(UUID uuid, String username) {
    this.uuid = uuid;
    this.username = username;
  }

  public void write(ByteBuf buf, int protocol) {
    if (protocol >= 735) { // 1.16+
      Buf.writeUuid(buf, uuid);
    } else if (protocol >= 5) { // 1.7.6+
      Buf.writeString(buf, uuid.toString());
    } else {
      Buf.writeString(buf, uuid.toString().replace("-", ""));
    }
    Buf.writeString(buf, username);
    if (protocol >= 759) { // 1.19+
      Buf.writeVarInt(buf, 0); // properties
    }
    if (protocol >= 766 && protocol < 768) { // 1.20.5-1.21.1
      buf.writeBoolean(true); // strict error handling
    }
  }
}

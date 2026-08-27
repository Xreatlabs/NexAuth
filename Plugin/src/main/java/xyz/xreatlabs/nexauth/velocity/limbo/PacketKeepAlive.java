/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Keepalive: long ≥1.12.2, varint 1.8-1.12.1, int below. */
public final class PacketKeepAlive {

  private final long id;

  public PacketKeepAlive(long id) {
    this.id = id;
  }

  public void write(ByteBuf buf, int protocol) {
    if (protocol >= 340) {
      buf.writeLong(id);
    } else if (protocol >= 47) {
      Buf.writeVarInt(buf, (int) id);
    } else {
      buf.writeInt((int) id);
    }
  }
}

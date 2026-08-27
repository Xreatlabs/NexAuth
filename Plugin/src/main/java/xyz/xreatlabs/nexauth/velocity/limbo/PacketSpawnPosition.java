/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Spawn position at (0, 400, 0); dimension name + second angle float for 1.21.9+. */
public final class PacketSpawnPosition {

  private final String dimensionName;

  public PacketSpawnPosition(String dimensionName) {
    this.dimensionName = dimensionName;
  }

  public void write(ByteBuf buf, int protocol) {
    if (protocol >= 773) {
      Buf.writeString(buf, dimensionName);
    }
    buf.writeLong(((0 & 0x3FFFFFF) << 38) | ((0 & 0x3FFFFFF) << 12) | (400 & 0xFFF));
    buf.writeFloat(0);
    if (protocol >= 773) {
      buf.writeFloat(0);
    }
  }
}

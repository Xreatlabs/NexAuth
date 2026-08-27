/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/**
 * Player position and look. Legacy path spawns at y=400 for 1.9+ (64 below, +1.62 eye height for
 * pre-1.8); ≥1.21.2 uses the modern relative-move shape.
 */
public final class PacketPositionLook {

  private final int teleportId;
  private final boolean legacyY;

  public PacketPositionLook(int teleportId, boolean legacyY) {
    this.teleportId = teleportId;
    this.legacyY = legacyY;
  }

  public void write(ByteBuf buf, int protocol) {
    if (protocol >= 768) { // 1.21.2+
      Buf.writeVarInt(buf, teleportId);
      buf.writeDouble(0);
      buf.writeDouble(400);
      buf.writeDouble(0);
      buf.writeDouble(0);
      buf.writeDouble(0);
      buf.writeDouble(0);
      buf.writeFloat(0);
      buf.writeFloat(0);
      buf.writeInt(0x08);
      return;
    }

    double y = legacyY ? 64 : 400;
    if (protocol < 47) {
      y += 1.62F;
    }
    buf.writeDouble(0);
    buf.writeDouble(y);
    buf.writeDouble(0);
    buf.writeFloat(0);
    buf.writeFloat(0);

    if (protocol >= 47) {
      buf.writeByte(0x08);
    } else {
      buf.writeBoolean(true);
    }

    if (protocol >= 107) {
      Buf.writeVarInt(buf, teleportId);
    }

    if (protocol >= 755 && protocol <= 761) {
      buf.writeBoolean(false); // dismount vehicle
    }
  }
}

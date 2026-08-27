/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;

/** Empty chunk data sent after start-waiting-chunks (1.20.3+), 9 chunks around origin. */
public final class PacketEmptyChunk {

  private static final byte[] LIGHT_DATA = {
    1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0,
  };

  private final int x;
  private final int z;

  public PacketEmptyChunk(int x, int z) {
    this.x = x;
    this.z = z;
  }

  public void write(ByteBuf buf, int protocol) {
    buf.writeInt(x);
    buf.writeInt(z);

    if (protocol >= 770) { // 1.21.5+: inline motion-blocking heightmap
      Buf.writeVarInt(buf, 1); // heightmap count
      Buf.writeVarInt(buf, 4); // motion-blocking type
      Buf.writeVarInt(buf, 37);
      for (int i = 0; i < 37; i++) {
        buf.writeLong(0);
      }
    } else {
      CompoundBinaryTag rootTag =
          CompoundBinaryTag.builder()
              .put(
                  "root",
                  CompoundBinaryTag.builder()
                      .put("MOTION_BLOCKING", LongArrayBinaryTag.longArrayBinaryTag(new long[37]))
                      .build())
              .build();
      Buf.writeCompoundTag(buf, rootTag, protocol);
    }

    Buf.writeVarInt(buf, 192); // section data length (24 sections × 8 bytes)
    for (int i = 0; i < 24; i++) {
      buf.writeZero(8);
    }

    Buf.writeVarInt(buf, 0); // block entities
    buf.writeBytes(LIGHT_DATA, 1, LIGHT_DATA.length - 1); // 14 raw light bytes
  }
}

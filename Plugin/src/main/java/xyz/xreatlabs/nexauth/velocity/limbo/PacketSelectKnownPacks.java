/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Known packs offer: a single vanilla {@code minecraft:core} pack at the client's own version. */
public final class PacketSelectKnownPacks {

  private final String version;

  public PacketSelectKnownPacks(String version) {
    this.version = version;
  }

  public void write(ByteBuf buf, int protocol) {
    Buf.writeVarInt(buf, 1);
    Buf.writeString(buf, "minecraft");
    Buf.writeString(buf, "core");
    Buf.writeString(buf, version);
  }
}

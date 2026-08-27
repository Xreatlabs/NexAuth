/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

/** Add-player player-info update, sent unconditionally to 1.16.4 (protocol 754). */
public final class PacketPlayerInfo {

  private final UUID uuid;
  private final String username;
  private final int gameMode;

  public PacketPlayerInfo(UUID uuid, String username, int gameMode) {
    this.uuid = uuid;
    this.username = username;
    this.gameMode = gameMode;
  }

  public void write(ByteBuf buf, int protocol) {
    if (protocol >= 761) { // 1.19.3+
      // Action bitmask over ordinals: ADD_PLAYER (0), UPDATE_GAMEMODE (2), UPDATE_LISTED (3)
      buf.writeByte(0b1101);
      Buf.writeVarInt(buf, 1); // one entry
      Buf.writeUuid(buf, uuid);
      Buf.writeString(buf, username);
      Buf.writeVarInt(buf, 0); // properties
      buf.writeBoolean(true); // listed
      Buf.writeVarInt(buf, gameMode);
      return;
    }

    Buf.writeVarInt(buf, 0); // add player
    Buf.writeVarInt(buf, 1); // one entry
    Buf.writeUuid(buf, uuid);
    Buf.writeString(buf, username);
    Buf.writeVarInt(buf, 0); // properties
    Buf.writeVarInt(buf, gameMode);
    Buf.writeVarInt(buf, 60); // ping
    buf.writeBoolean(false); // no display name
    if (protocol >= 759) { // 1.19+ signature data
      buf.writeBoolean(false);
    }
  }
}

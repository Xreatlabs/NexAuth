/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.nbt.CompoundBinaryTag;

/**
 * JoinGame payload builder for every supported era, ported from the vendored limbo with its config
 * values in force: entityId 0, gamemode 2 (adventure), the_end dimension, viewDistance 0,
 * hardcore/debug/flat/limitedCrafting/secureProfile false, hashedSeed 0, respawnScreen true.
 */
public final class JoinGameData {

  public static final String DIMENSION_NAME = "minecraft:the_end";

  private final RegistryData registryData;

  public JoinGameData(RegistryData registryData) {
    this.registryData = registryData;
  }

  /** Legacy (pre-1.16) numeric the_end id. */
  private static final int LEGACY_THE_END_ID = 1;

  public void write(ByteBuf buf, int protocol) {
    buf.writeInt(0); // entityId

    if (protocol >= 4 && protocol <= 5) { // 1.7.2-1.7.10
      buf.writeByte(2); // gamemode (3→1 mapping irrelevant for 2)
      buf.writeByte(LEGACY_THE_END_ID);
      buf.writeByte(0); // difficulty
      buf.writeByte(maxPlayers());
      Buf.writeString(buf, "flat");
      return;
    }

    if (protocol >= 47 && protocol <= 107) { // 1.8-1.9
      buf.writeByte(2);
      buf.writeByte(LEGACY_THE_END_ID);
      buf.writeByte(0);
      buf.writeByte(maxPlayers());
      Buf.writeString(buf, "flat");
      buf.writeBoolean(true); // reduced debug
      return;
    }

    if (protocol >= 108 && protocol <= 404) { // 1.9.1-1.13.2
      buf.writeByte(2);
      buf.writeInt(LEGACY_THE_END_ID);
      buf.writeByte(0);
      buf.writeByte(maxPlayers());
      Buf.writeString(buf, "flat");
      buf.writeBoolean(true);
      return;
    }

    if (protocol >= 477 && protocol <= 498) { // 1.14.x
      buf.writeByte(2);
      buf.writeInt(LEGACY_THE_END_ID);
      buf.writeByte(maxPlayers());
      Buf.writeString(buf, "flat");
      Buf.writeVarInt(buf, 0); // view distance
      buf.writeBoolean(true);
      return;
    }

    if (protocol >= 573 && protocol <= 578) { // 1.15.x
      buf.writeByte(2);
      buf.writeInt(LEGACY_THE_END_ID);
      buf.writeLong(0); // hashed seed
      buf.writeByte(maxPlayers());
      Buf.writeString(buf, "flat");
      Buf.writeVarInt(buf, 0);
      buf.writeBoolean(true);
      buf.writeBoolean(true); // respawn screen
      return;
    }

    if (protocol >= 735 && protocol <= 736) { // 1.16-1.16.1
      buf.writeByte(2);
      buf.writeByte(-1); // previous gamemode
      writeWorldNames(buf);
      Buf.writeCompoundTag(buf, registryData.codecPlay(protocol), protocol);
      Buf.writeString(buf, DIMENSION_NAME);
      Buf.writeString(buf, DIMENSION_NAME);
      buf.writeLong(0);
      buf.writeByte(maxPlayers());
      Buf.writeVarInt(buf, 0);
      buf.writeBoolean(true);
      buf.writeBoolean(true);
      buf.writeBoolean(false); // debug
      buf.writeBoolean(false); // flat
      return;
    }

    if (protocol >= 751 && protocol <= 758) { // 1.16.2-1.18.2
      buf.writeBoolean(false); // hardcore
      buf.writeByte(2);
      buf.writeByte(-1);
      writeWorldNames(buf);
      CompoundBinaryTag codec = registryData.codecPlay(protocol);
      Buf.writeCompoundTag(buf, codec, protocol);
      Buf.writeCompoundTag(buf, registryData.dimensionElement(protocol, DIMENSION_NAME), protocol);
      Buf.writeString(buf, DIMENSION_NAME); // world name
      buf.writeLong(0);
      Buf.writeVarInt(buf, maxPlayers());
      Buf.writeVarInt(buf, 0); // view distance
      if (protocol >= 757) {
        Buf.writeVarInt(buf, 0); // simulation distance
      }
      buf.writeBoolean(true);
      buf.writeBoolean(true);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      return;
    }

    if (protocol >= 759 && protocol <= 762) { // 1.19-1.19.4
      buf.writeBoolean(false);
      buf.writeByte(2);
      buf.writeByte(-1);
      writeWorldNames(buf);
      Buf.writeCompoundTag(buf, registryData.codecPlay(protocol), protocol);
      Buf.writeString(buf, DIMENSION_NAME); // world type
      Buf.writeString(buf, DIMENSION_NAME);
      buf.writeLong(0);
      Buf.writeVarInt(buf, maxPlayers());
      Buf.writeVarInt(buf, 0);
      Buf.writeVarInt(buf, 0);
      buf.writeBoolean(true);
      buf.writeBoolean(true);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      return;
    }

    if (protocol == 763) { // 1.20/1.20.1
      buf.writeBoolean(false);
      buf.writeByte(2);
      buf.writeByte(-1);
      writeWorldNames(buf);
      Buf.writeCompoundTag(buf, registryData.codecPlay(protocol), protocol);
      Buf.writeString(buf, DIMENSION_NAME);
      Buf.writeString(buf, DIMENSION_NAME);
      buf.writeLong(0);
      Buf.writeVarInt(buf, maxPlayers());
      Buf.writeVarInt(buf, 0);
      Buf.writeVarInt(buf, 0);
      buf.writeBoolean(true);
      buf.writeBoolean(true);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      Buf.writeVarInt(buf, 0);
      return;
    }

    if (protocol == 764 || protocol == 765) { // 1.20.2-1.20.3: no NBT
      buf.writeBoolean(false);
      writeWorldNames(buf);
      Buf.writeVarInt(buf, maxPlayers());
      Buf.writeVarInt(buf, 0);
      Buf.writeVarInt(buf, 0);
      buf.writeBoolean(true);
      buf.writeBoolean(true);
      buf.writeBoolean(false); // limited crafting
      Buf.writeString(buf, DIMENSION_NAME);
      Buf.writeString(buf, DIMENSION_NAME);
      buf.writeLong(0);
      buf.writeByte(2);
      buf.writeByte(-1);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      buf.writeBoolean(false);
      Buf.writeVarInt(buf, 0);
      return;
    }

    // 766+ (1.20.5+): dimension varint id, secureProfile flag at end
    buf.writeBoolean(false);
    writeWorldNames(buf);
    Buf.writeVarInt(buf, maxPlayers());
    Buf.writeVarInt(buf, 0);
    Buf.writeVarInt(buf, 0);
    buf.writeBoolean(true);
    buf.writeBoolean(true);
    buf.writeBoolean(false);
    Buf.writeVarInt(buf, registryData.dimensionId(protocol, DIMENSION_NAME));
    Buf.writeString(buf, DIMENSION_NAME);
    buf.writeLong(0);
    buf.writeByte(2);
    buf.writeByte(-1);
    buf.writeBoolean(false);
    buf.writeBoolean(false);
    buf.writeBoolean(false);
    Buf.writeVarInt(buf, 0);
    if (protocol >= 768) {
      Buf.writeVarInt(buf, 0);
    }
    buf.writeBoolean(false); // secure profile
  }

  private void writeWorldNames(ByteBuf buf) {
    Buf.writeVarInt(buf, 1);
    Buf.writeString(buf, DIMENSION_NAME);
  }

  private int maxPlayers() {
    return 100;
  }
}

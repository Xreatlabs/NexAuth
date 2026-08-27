/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.kyori.adventure.nbt.CompoundBinaryTag;

/**
 * Configuration-state registry data. Two shapes: 764-765 sends a single verbatim codec_1_20 NBT;
 * ≥766 sends one packet per top-level registry of the version's codec.
 */
public final class PacketRegistryData {

  private final RegistryData registryData;
  private final int protocol;

  public PacketRegistryData(RegistryData registryData, int protocol) {
    this.registryData = registryData;
    this.protocol = protocol;
  }

  public void writeWhole(ByteBuf buf, int wireProtocol) {
    Buf.writeCompoundTag(buf, registryData.codecConfigurationLegacy(wireProtocol), wireProtocol);
  }

  /**
   * Writes the packet body for a single registry type: string type, varint count, then
   * name/hasElement/element tuples.
   */
  public void writeRegistry(ByteBuf buf, String registryType) {
    List<RegistryData.Entry> entries =
        registryData.registryEntries(registryData.codecConfigurationModern(protocol), registryType);
    Buf.writeString(buf, registryType);
    Buf.writeVarInt(buf, entries.size());
    for (RegistryData.Entry entry : entries) {
      Buf.writeString(buf, entry.name());
      CompoundBinaryTag element = entry.element();
      if (element != null) {
        buf.writeBoolean(true);
        Buf.writeCompoundTag(buf, element, protocol);
      } else {
        buf.writeBoolean(false);
      }
    }
  }
}

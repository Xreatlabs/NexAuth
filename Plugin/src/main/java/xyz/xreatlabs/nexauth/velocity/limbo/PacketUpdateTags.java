/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.nbt.CompoundBinaryTag;

/** Update-tags packet on the known-packs (≥766) configuration path. */
public final class PacketUpdateTags {

  private final RegistryData registryData;
  private final int protocol;

  public PacketUpdateTags(RegistryData registryData, int protocol) {
    this.registryData = registryData;
    this.protocol = protocol;
  }

  public void write(ByteBuf buf, int wireProtocol) {
    CompoundBinaryTag tags = registryData.tags(protocol);
    registryData.writeTags(buf, tags);
  }
}

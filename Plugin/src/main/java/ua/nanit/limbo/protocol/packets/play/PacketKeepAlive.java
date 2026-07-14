/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketKeepAlive implements PacketIn, PacketOut {

  private long id;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    if (version.moreOrEqual(Version.V1_12_2)) {
      msg.writeLong(id);
    } else if (version.moreOrEqual(Version.V1_8)) {
      msg.writeVarInt((int) id);
    } else {
      msg.writeInt((int) id);
    }
  }

  @Override
  public void decode(ByteMessage msg, Version version) {
    if (version.moreOrEqual(Version.V1_12_2)) {
      this.id = msg.readLong();
    } else if (version.moreOrEqual(Version.V1_8)) {
      this.id = msg.readVarInt();
    } else {
      this.id = msg.readInt();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

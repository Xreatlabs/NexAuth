/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import java.util.UUID;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketLoginSuccess implements PacketOut {

  private UUID uuid;
  private String username;

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    if (version.moreOrEqual(Version.V1_16)) {
      msg.writeUuid(uuid);
    } else if (version.moreOrEqual(Version.V1_7_6)) {
      msg.writeString(uuid.toString());
    } else {
      msg.writeString(uuid.toString().replace("-", ""));
    }
    msg.writeString(username);
    if (version.moreOrEqual(Version.V1_19)) {
      msg.writeVarInt(0);
    }
    if (version.fromTo(Version.V1_20_5, Version.V1_21)) {
      msg.writeBoolean(true);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import java.util.UUID;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.data.BossBar;

/** Packet for 1.9+ */
public class PacketBossBar implements PacketOut {

  private UUID uuid;
  private BossBar bossBar;
  private int flags;

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public void setBossBar(BossBar bossBar) {
    this.bossBar = bossBar;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeUuid(uuid);
    msg.writeVarInt(0); // Create bossbar
    msg.writeNbtMessage(bossBar.getText(), version);
    msg.writeFloat(bossBar.getHealth());
    msg.writeVarInt(bossBar.getColor().getIndex());
    msg.writeVarInt(bossBar.getDivision().getIndex());
    msg.writeByte(flags);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

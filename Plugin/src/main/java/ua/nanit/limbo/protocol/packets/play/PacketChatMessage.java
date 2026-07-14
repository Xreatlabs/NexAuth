/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import java.util.UUID;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.NbtMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketChatMessage implements PacketOut {

  private NbtMessage message;
  private PositionLegacy position;
  private UUID sender;

  public void setMessage(NbtMessage message) {
    this.message = message;
  }

  public void setPosition(PositionLegacy position) {
    this.position = position;
  }

  public void setSender(UUID sender) {
    this.sender = sender;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeNbtMessage(message, version);
    if (version.moreOrEqual(Version.V1_19_1)) {
      msg.writeBoolean(position.index == PositionLegacy.ACTION_BAR.index);
    } else if (version.moreOrEqual(Version.V1_19)) {
      msg.writeVarInt(position.index);
    } else if (version.moreOrEqual(Version.V1_8)) {
      msg.writeByte(position.index);
    }

    if (version.moreOrEqual(Version.V1_16) && version.less(Version.V1_19)) msg.writeUuid(sender);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public enum PositionLegacy {
    CHAT(0),
    SYSTEM_MESSAGE(1),
    ACTION_BAR(2);

    private final int index;

    PositionLegacy(int index) {
      this.index = index;
    }
  }
}

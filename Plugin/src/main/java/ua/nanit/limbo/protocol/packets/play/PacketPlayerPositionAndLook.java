/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketPlayerPositionAndLook implements PacketOut {

  private double x;
  private double y;
  private double z;
  private float yaw;
  private float pitch;
  private int teleportId;

  public PacketPlayerPositionAndLook() {}

  public PacketPlayerPositionAndLook(
      double x, double y, double z, float yaw, float pitch, int teleportId) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
    this.teleportId = teleportId;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    if (version.moreOrEqual(Version.V1_21_2)) {
      encodeModern(msg, version);
      return;
    }

    encodeLegacy(msg, version);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void encodeLegacy(ByteMessage msg, Version version) {
    msg.writeDouble(x);
    msg.writeDouble(y + (version.less(Version.V1_8) ? 1.62F : 0));
    msg.writeDouble(z);
    msg.writeFloat(yaw);
    msg.writeFloat(pitch);

    if (version.moreOrEqual(Version.V1_8)) {
      msg.writeByte(0x08);
    } else {
      msg.writeBoolean(true);
    }

    if (version.moreOrEqual(Version.V1_9)) {
      msg.writeVarInt(teleportId);
    }

    if (version.fromTo(Version.V1_17, Version.V1_19_3)) {
      msg.writeBoolean(false); // Dismount vehicle
    }
  }

  private void encodeModern(ByteMessage msg, Version version) {
    msg.writeVarInt(teleportId);

    msg.writeDouble(x);
    msg.writeDouble(y);
    msg.writeDouble(z);

    msg.writeDouble(0);
    msg.writeDouble(0);
    msg.writeDouble(0);

    msg.writeFloat(yaw);
    msg.writeFloat(pitch);

    msg.writeInt(0x08);
  }
}

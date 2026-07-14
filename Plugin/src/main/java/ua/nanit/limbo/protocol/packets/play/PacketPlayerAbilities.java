/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketPlayerAbilities implements PacketOut {

  private int flags = 0x02;
  private float flyingSpeed = 0.0F;
  private float fieldOfView = 0.1F;

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public void setFlyingSpeed(float flyingSpeed) {
    this.flyingSpeed = flyingSpeed;
  }

  public void setFieldOfView(float fieldOfView) {
    this.fieldOfView = fieldOfView;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeByte(flags);
    msg.writeFloat(flyingSpeed);
    msg.writeFloat(fieldOfView);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

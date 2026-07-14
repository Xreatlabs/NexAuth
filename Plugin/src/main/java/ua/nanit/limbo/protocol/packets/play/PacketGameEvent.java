/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketGameEvent implements PacketOut {

  private byte type;
  private float value;

  public void setType(byte type) {
    this.type = type;
  }

  public void setValue(float value) {
    this.value = value;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeByte(type);
    msg.writeFloat(value);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

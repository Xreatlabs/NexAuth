/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketTitleTimes implements PacketOut {

  private int fadeIn;
  private int stay;
  private int fadeOut;

  public void setFadeIn(int fadeIn) {
    this.fadeIn = fadeIn;
  }

  public void setStay(int stay) {
    this.stay = stay;
  }

  public void setFadeOut(int fadeOut) {
    this.fadeOut = fadeOut;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeInt(fadeIn);
    msg.writeInt(stay);
    msg.writeInt(fadeOut);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.NbtMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketTitleSetSubTitle implements PacketOut {

  private NbtMessage subtitle;

  public void setSubtitle(NbtMessage subtitle) {
    this.subtitle = subtitle;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeNbtMessage(subtitle, version);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

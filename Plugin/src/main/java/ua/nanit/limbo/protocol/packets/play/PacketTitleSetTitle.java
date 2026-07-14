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

public class PacketTitleSetTitle implements PacketOut {

  private NbtMessage title;

  public void setTitle(NbtMessage title) {
    this.title = title;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeNbtMessage(title, version);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

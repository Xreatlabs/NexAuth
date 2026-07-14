/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketDisconnect implements PacketOut {

  private String reason;

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeString(String.format("{\"text\": \"%s\"}", reason));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.server.LimboServer;

public class PacketLoginAcknowledged implements PacketIn, PacketOut {

  @Override
  public void handle(ClientConnection conn, LimboServer server) {
    server.getPacketHandler().handle(conn, this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.status;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public class PacketStatusRequest implements PacketIn {

  @Override
  public void decode(ByteMessage msg, Version version) {}

  @Override
  public void handle(ClientConnection conn, LimboServer server) {
    server.getPacketHandler().handle(conn, this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

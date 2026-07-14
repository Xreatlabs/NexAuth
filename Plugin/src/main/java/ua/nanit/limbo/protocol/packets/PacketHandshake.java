/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public class PacketHandshake implements PacketIn {

  private Version version;
  private String host;
  private int port;
  private State nextState;

  public Version getVersion() {
    return version;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public State getNextState() {
    return nextState;
  }

  @Override
  public void decode(ByteMessage msg, Version version) {
    try {
      this.version = Version.of(msg.readVarInt());
    } catch (IllegalArgumentException e) {
      this.version = Version.UNDEFINED;
    }

    this.host = msg.readString();
    this.port = msg.readUnsignedShort();
    this.nextState = State.getById(msg.readVarInt());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void handle(ClientConnection conn, LimboServer server) {
    server.getPacketHandler().handle(conn, this);
  }
}

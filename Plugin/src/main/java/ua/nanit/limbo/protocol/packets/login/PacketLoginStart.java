/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import java.util.UUID;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.PlayerPublicKey;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public class PacketLoginStart implements PacketIn {

  private String username;
  private PlayerPublicKey playerPublicKey;
  private UUID uuid;

  public String getUsername() {
    return username;
  }

  public PlayerPublicKey getPlayerPublicKey() {
    return playerPublicKey;
  }

  public UUID getUuid() {
    return uuid;
  }

  @Override
  public void decode(ByteMessage msg, Version version) {
    this.username = msg.readString(256);

    if (version.fromTo(Version.V1_19, Version.V1_19_1)) {
      this.playerPublicKey = msg.readPublicKey();
    }

    if (version.moreOrEqual(Version.V1_19_1)) {
      if (version.moreOrEqual(Version.V1_20_2) || msg.readBoolean()) {
        this.uuid = msg.readUuid();
      }
    }
  }

  @Override
  public void handle(ClientConnection conn, LimboServer server) {
    server.getPacketHandler().handle(conn, this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

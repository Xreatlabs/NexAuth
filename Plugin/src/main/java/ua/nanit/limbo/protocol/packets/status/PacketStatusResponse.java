/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.status;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public class PacketStatusResponse implements PacketOut {

  private static final String TEMPLATE =
      "{ \"version\": { \"name\": \"%s\", \"protocol\": %d }, \"players\": { \"max\": %d,"
          + " \"online\": %d, \"sample\": [] }, \"description\": %s }";

  private LimboServer server;

  public PacketStatusResponse() {}

  public PacketStatusResponse(LimboServer server) {
    this.server = server;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    int protocol;
    int staticProtocol = server.getConfig().getPingData().getProtocol();

    if (staticProtocol > 0) {
      protocol = staticProtocol;
    } else {
      protocol =
          server.getConfig().getInfoForwarding().isNone()
              ? version.getProtocolNumber()
              : Version.getMax().getProtocolNumber();
    }

    String ver = server.getConfig().getPingData().getVersion();
    String desc = server.getConfig().getPingData().getDescription();

    msg.writeString(
        getResponseJson(
            ver,
            protocol,
            server.getConfig().getMaxPlayers(),
            server.getConnections().getCount(),
            desc));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private String getResponseJson(
      String version, int protocol, int maxPlayers, int online, String description) {
    return String.format(TEMPLATE, version, protocol, maxPlayers, online, description);
  }
}

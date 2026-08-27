/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Server list ping response; description substituted unquoted, matching the vendored limbo. */
public final class PacketStatusResponse {

  private static final String TEMPLATE =
      "{ \"version\": { \"name\": \"%s\", \"protocol\": %d }, \"players\": { \"max\": %d,"
          + " \"online\": %d, \"sample\": [] }, \"description\": %s }";

  private final String versionName;
  private final int protocol;
  private final int maxPlayers;
  private final int online;
  private final String description;

  public PacketStatusResponse(
      String versionName, int protocol, int maxPlayers, int online, String description) {
    this.versionName = versionName;
    this.protocol = protocol;
    this.maxPlayers = maxPlayers;
    this.online = online;
    this.description = description;
  }

  public void write(ByteBuf buf, int wireProtocol) {
    Buf.writeString(
        buf, String.format(TEMPLATE, versionName, protocol, maxPlayers, online, description));
  }
}

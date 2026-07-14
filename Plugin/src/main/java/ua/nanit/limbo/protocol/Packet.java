/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public interface Packet {

  void encode(ByteMessage msg, Version version);

  void decode(ByteMessage msg, Version version);

  default void handle(ClientConnection conn, LimboServer server) {
    // Ignored by default
  }
}

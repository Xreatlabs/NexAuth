/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ua.nanit.limbo.connection.ClientConnection;

public final class Connections {

  private final Map<UUID, ClientConnection> connections;

  public Connections() {
    connections = new ConcurrentHashMap<>();
  }

  public Collection<ClientConnection> getAllConnections() {
    return Collections.unmodifiableCollection(connections.values());
  }

  public int getCount() {
    return connections.size();
  }

  public void addConnection(ClientConnection connection) {
    connections.put(connection.getUuid(), connection);
    Log.info(
        "Player %s connected (%s) [%s]",
        connection.getUsername(), connection.getAddress(), connection.getClientVersion());
  }

  public void removeConnection(ClientConnection connection) {
    connections.remove(connection.getUuid());
    Log.info("Player %s disconnected", connection.getUsername());
  }
}

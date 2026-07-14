/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ServerPingTest {

  @Test
  void storesMaxPlayers() {
    var ping = new ServerPing(250);

    assertEquals(250, ping.maxPlayers());
    assertEquals(new ServerPing(250), ping);
  }
}

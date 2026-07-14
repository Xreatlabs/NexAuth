/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PingDataTest {

  @Test
  void storesConfiguredPingFields() {
    var pingData = new PingData();
    pingData.setDescription("desc");
    pingData.setVersion("1.21");
    pingData.setProtocol(767);

    assertEquals("desc", pingData.getDescription());
    assertEquals("1.21", pingData.getVersion());
    assertEquals(767, pingData.getProtocol());
  }
}

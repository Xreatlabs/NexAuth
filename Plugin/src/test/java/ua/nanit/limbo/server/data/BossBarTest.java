/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import ua.nanit.limbo.protocol.NbtMessage;

class BossBarTest {

  @Test
  void storesBossBarFieldsAndEnumIndexes() {
    var bossBar = new BossBar();
    var text = new NbtMessage("{\"text\":\"Boss\"}", null);

    bossBar.setText(text);
    bossBar.setHealth(0.5f);
    bossBar.setColor(BossBar.Color.PURPLE);
    bossBar.setDivision(BossBar.Division.DASHES_10);

    assertSame(text, bossBar.getText());
    assertEquals(0.5f, bossBar.getHealth());
    assertEquals(BossBar.Color.PURPLE, bossBar.getColor());
    assertEquals(5, BossBar.Color.PURPLE.getIndex());
    assertEquals(BossBar.Division.DASHES_10, bossBar.getDivision());
    assertEquals(2, BossBar.Division.DASHES_10.getIndex());
  }
}

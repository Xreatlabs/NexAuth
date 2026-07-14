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

class TitleTest {

  @Test
  void storesTitleFields() {
    var title = new Title();
    var titleMessage = new NbtMessage("{\"text\":\"Hello\"}", null);
    var subtitleMessage = new NbtMessage("{\"text\":\"World\"}", null);

    title.setTitle(titleMessage);
    title.setSubtitle(subtitleMessage);
    title.setFadeIn(10);
    title.setStay(60);
    title.setFadeOut(20);

    assertSame(titleMessage, title.getTitle());
    assertSame(subtitleMessage, title.getSubtitle());
    assertEquals(10, title.getFadeIn());
    assertEquals(60, title.getStay());
    assertEquals(20, title.getFadeOut());
  }
}

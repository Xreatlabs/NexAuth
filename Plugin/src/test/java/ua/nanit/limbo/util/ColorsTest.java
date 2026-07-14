/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ColorsTest {

  @Test
  void replacesAmpersandColorCodes() {
    assertEquals("§aHello §bWorld", Colors.of("&aHello &bWorld"));
  }

  @Test
  void preservesNull() {
    assertNull(Colors.of(null));
  }
}

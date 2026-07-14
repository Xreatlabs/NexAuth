/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BiHolderTest {

  @Test
  void storesKeyAndValue() {
    var holder = new BiHolder<>("mode", 7);

    assertEquals("mode", holder.key());
    assertEquals(7, holder.value());
    assertEquals(new BiHolder<>("mode", 7), holder);
  }
}

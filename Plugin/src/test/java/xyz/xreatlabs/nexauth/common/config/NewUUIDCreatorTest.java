/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NewUUIDCreatorTest {

  @Test
  void exposesExpectedStrategies() {
    assertArrayEquals(
        new NewUUIDCreator[] {NewUUIDCreator.RANDOM, NewUUIDCreator.MOJANG, NewUUIDCreator.CRACKED},
        NewUUIDCreator.values());
    assertEquals(NewUUIDCreator.MOJANG, NewUUIDCreator.valueOf("MOJANG"));
  }
}

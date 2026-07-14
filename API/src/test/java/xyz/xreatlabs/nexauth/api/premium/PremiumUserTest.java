/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.premium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PremiumUserTest {

  @Test
  void storesPremiumIdentityData() {
    var uuid = UUID.randomUUID();
    var user = new PremiumUser(uuid, "Kartvya69", false);

    assertEquals(uuid, user.uuid());
    assertEquals("Kartvya69", user.name());
    assertFalse(user.reliable());
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class PreLoginResultTest {

  @Test
  void storesStateMessageAndNullableUser() {
    var message = Component.text("Denied");
    var result = new PreLoginResult(PreLoginState.DENIED, message, null);

    assertEquals(PreLoginState.DENIED, result.state());
    assertEquals(message, result.message());
    assertNull(result.user());
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.listener;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PreLoginStateTest {

  @Test
  void exposesExpectedOrderingAndNames() {
    assertArrayEquals(
        new PreLoginState[] {
          PreLoginState.FORCE_ONLINE, PreLoginState.FORCE_OFFLINE, PreLoginState.DENIED
        },
        PreLoginState.values());
    assertEquals(PreLoginState.DENIED, PreLoginState.valueOf("DENIED"));
  }
}

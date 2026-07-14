/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LogTest {

  @Test
  void debugFlagFollowsConfiguredLevel() {
    Log.setLevel(Log.Level.INFO.getIndex());
    assertFalse(Log.isDebug());

    Log.setLevel(Log.Level.DEBUG.getIndex());
    assertTrue(Log.isDebug());
  }
}

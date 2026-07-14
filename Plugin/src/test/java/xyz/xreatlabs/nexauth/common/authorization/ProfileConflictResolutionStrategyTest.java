/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.authorization;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProfileConflictResolutionStrategyTest {

  @Test
  void exposesExpectedStrategies() {
    assertArrayEquals(
        new ProfileConflictResolutionStrategy[] {
          ProfileConflictResolutionStrategy.BLOCK,
          ProfileConflictResolutionStrategy.USE_OFFLINE,
          ProfileConflictResolutionStrategy.OVERWRITE
        },
        ProfileConflictResolutionStrategy.values());
    assertEquals(
        ProfileConflictResolutionStrategy.OVERWRITE,
        ProfileConflictResolutionStrategy.valueOf("OVERWRITE"));
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.reliability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FailurePolicyModeTest {

  @Test
  void parseNullFallsBackToHardFail() {
    assertEquals(FailurePolicyMode.HARD_FAIL, FailurePolicyMode.parse(null));
  }

  @Test
  void parseInvalidFallsBackToHardFail() {
    assertEquals(FailurePolicyMode.HARD_FAIL, FailurePolicyMode.parse("invalid"));
  }

  @Test
  void parseKnownValueWorksCaseInsensitive() {
    assertEquals(
        FailurePolicyMode.RETRY_THEN_DISABLE, FailurePolicyMode.parse("retry_then_disable"));
  }
}

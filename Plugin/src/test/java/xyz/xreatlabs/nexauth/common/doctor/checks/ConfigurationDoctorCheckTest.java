/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.common.doctor.DoctorSeverity;

class ConfigurationDoctorCheckTest {

  @Test
  void warnsWhenFailurePolicyModeIsUnknown() {
    var result = new ConfigurationDoctorCheck("mystery-mode").run();

    assertEquals("configuration", result.checkId());
    assertEquals(DoctorSeverity.WARN, result.severity());
    assertEquals("Unknown failure policy mode 'mystery-mode', using HARD_FAIL", result.message());
  }

  @Test
  void reportsOkWhenFailurePolicyModeIsRecognized() {
    var result = new ConfigurationDoctorCheck("DEGRADE").run();

    assertEquals(DoctorSeverity.OK, result.severity());
    assertEquals("Failure policy mode set to DEGRADE", result.message());
  }
}

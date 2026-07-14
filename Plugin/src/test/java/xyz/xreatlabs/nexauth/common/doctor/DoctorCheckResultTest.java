/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DoctorCheckResultTest {

  @Test
  void severityUsesExpectedOrdering() {
    assertTrue(DoctorSeverity.OK.compareTo(DoctorSeverity.WARN) < 0);
    assertTrue(DoctorSeverity.WARN.compareTo(DoctorSeverity.FAIL) < 0);
  }

  @Test
  void compareToSortsBySeverityThenCheckId() {
    var fail = new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable");
    var warn =
        new DoctorCheckResult("config", DoctorSeverity.WARN, "Configuration fallback active");
    var ok = new DoctorCheckResult("email", DoctorSeverity.OK, "Email ready");
    var warnLater =
        new DoctorCheckResult("premium", DoctorSeverity.WARN, "Premium provider reachable");

    var sorted = java.util.stream.Stream.of(ok, warnLater, fail, warn).sorted().toList();

    assertEquals(java.util.List.of(fail, warn, warnLater, ok), sorted);
  }
}

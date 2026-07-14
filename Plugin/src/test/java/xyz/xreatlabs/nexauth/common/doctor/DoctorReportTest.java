/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DoctorReportTest {

  @Test
  void derivesSummaryCountsAndOverallSeverity() {
    var report =
        new DoctorReport(
            List.of(
                new DoctorCheckResult("email", DoctorSeverity.OK, "Email ready"),
                new DoctorCheckResult(
                    "config", DoctorSeverity.WARN, "Configuration fallback active"),
                new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable"),
                new DoctorCheckResult("premium", DoctorSeverity.WARN, "Premium provider slow")));

    assertEquals(DoctorSeverity.FAIL, report.overallSeverity());
    assertEquals(1, report.okCount());
    assertEquals(2, report.warnCount());
    assertEquals(1, report.failCount());
  }

  @Test
  void sortsChecksDeterministically() {
    var ok = new DoctorCheckResult("email", DoctorSeverity.OK, "Email ready");
    var warn =
        new DoctorCheckResult("config", DoctorSeverity.WARN, "Configuration fallback active");
    var fail = new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable");
    var warnLater =
        new DoctorCheckResult("premium", DoctorSeverity.WARN, "Premium provider reachable");

    var report = new DoctorReport(List.of(ok, warnLater, fail, warn));

    assertEquals(List.of(fail, warn, warnLater, ok), report.checks());
  }

  @Test
  void emptyReportDefaultsToOkSeverity() {
    var report = new DoctorReport(List.of());

    assertEquals(DoctorSeverity.OK, report.overallSeverity());
    assertEquals(0, report.okCount());
    assertEquals(0, report.warnCount());
    assertEquals(0, report.failCount());
  }
}

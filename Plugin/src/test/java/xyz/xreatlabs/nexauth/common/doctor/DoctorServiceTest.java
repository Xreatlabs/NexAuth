/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DoctorServiceTest {

  @Test
  void runsChecksIntoOrderedReport() {
    var service =
        new DoctorService(
            List.<DoctorCheck>of(
                () -> new DoctorCheckResult("email", DoctorSeverity.OK, "Email ready"),
                () ->
                    new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable"),
                () ->
                    new DoctorCheckResult(
                        "config", DoctorSeverity.WARN, "Configuration fallback active")));

    var report = service.run();

    assertEquals(DoctorSeverity.FAIL, report.overallSeverity());
    assertEquals(
        List.of("database", "config", "email"),
        report.checks().stream().map(DoctorCheckResult::checkId).toList());
  }
}

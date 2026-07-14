/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DoctorRendererTest {

  @Test
  void rendersOrderedDoctorResultsAndSummary() {
    var report =
        new DoctorReport(
            List.of(
                new DoctorCheckResult("email", DoctorSeverity.OK, "Email ready"),
                new DoctorCheckResult(
                    "config", DoctorSeverity.WARN, "Configuration fallback active"),
                new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable")));

    var rendered = DoctorRenderer.render(report);

    assertEquals(
        List.of(
            new RenderedLine("info-doctor-header", "%severity%", "FAIL"),
            new RenderedLine(
                "info-doctor-entry",
                "%status%",
                "FAIL",
                "%check%",
                "database",
                "%message%",
                "Database unreachable"),
            new RenderedLine(
                "info-doctor-entry",
                "%status%",
                "WARN",
                "%check%",
                "config",
                "%message%",
                "Configuration fallback active"),
            new RenderedLine(
                "info-doctor-entry",
                "%status%",
                "OK",
                "%check%",
                "email",
                "%message%",
                "Email ready"),
            new RenderedLine("info-doctor-summary", "%ok%", "1", "%warn%", "1", "%fail%", "1")),
        rendered);
  }

  @Test
  void verboseRenderingMatchesNormalRenderingUntilDetailsExist() {
    var report =
        new DoctorReport(
            List.of(
                new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable")));

    assertEquals(DoctorRenderer.render(report), DoctorRenderer.renderVerbose(report));
  }
}

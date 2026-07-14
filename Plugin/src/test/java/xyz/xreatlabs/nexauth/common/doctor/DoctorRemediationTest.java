/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DoctorRemediationTest {

  @Test
  void verboseRendererIncludesDetailAndRemediationLines() {
    var report =
        new DoctorReport(
            List.of(
                new DoctorCheckResult(
                    "database",
                    DoctorSeverity.FAIL,
                    "Database connector is disconnected",
                    "Observed connector state: disconnected",
                    "Verify database credentials and restart NexAuth")));

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
                "Database connector is disconnected"),
            new RenderedLine(
                "info-doctor-detail", "%detail%", "Observed connector state: disconnected"),
            new RenderedLine(
                "info-doctor-detail",
                "%detail%",
                "Remediation: Verify database credentials and restart NexAuth"),
            new RenderedLine("info-doctor-summary", "%ok%", "0", "%warn%", "0", "%fail%", "1")),
        DoctorRenderer.renderVerbose(report));
  }
}

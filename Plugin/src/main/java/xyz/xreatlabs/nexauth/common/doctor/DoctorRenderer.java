/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import java.util.ArrayList;
import java.util.List;

public final class DoctorRenderer {

  private DoctorRenderer() {}

  public static List<RenderedLine> render(DoctorReport report) {
    return render(report, false);
  }

  public static List<RenderedLine> renderVerbose(DoctorReport report) {
    return render(report, true);
  }

  private static List<RenderedLine> render(DoctorReport report, boolean verbose) {
    var lines = new ArrayList<RenderedLine>();
    lines.add(
        new RenderedLine("info-doctor-header", "%severity%", report.overallSeverity().name()));
    for (DoctorCheckResult check : report.checks()) {
      lines.add(
          new RenderedLine(
              "info-doctor-entry",
              "%status%",
              check.severity().name(),
              "%check%",
              check.checkId(),
              "%message%",
              check.message()));
      if (verbose) {
        if (check.detail() != null && !check.detail().isBlank()) {
          lines.add(new RenderedLine("info-doctor-detail", "%detail%", check.detail()));
        }
        if (check.remediation() != null && !check.remediation().isBlank()) {
          lines.add(
              new RenderedLine(
                  "info-doctor-detail", "%detail%", "Remediation: " + check.remediation()));
        }
      }
    }
    lines.add(
        new RenderedLine(
            "info-doctor-summary",
            "%ok%",
            Long.toString(report.okCount()),
            "%warn%",
            Long.toString(report.warnCount()),
            "%fail%",
            Long.toString(report.failCount())));
    return List.copyOf(lines);
  }
}

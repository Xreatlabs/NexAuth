/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import java.util.List;
import java.util.Objects;

public record DoctorReport(List<DoctorCheckResult> checks) {

  public DoctorReport {
    Objects.requireNonNull(checks, "checks");
    checks = checks.stream().sorted().toList();
  }

  public DoctorSeverity overallSeverity() {
    return checks.stream()
        .map(DoctorCheckResult::severity)
        .reduce(DoctorSeverity.OK, DoctorSeverity::max);
  }

  public long okCount() {
    return countBySeverity(DoctorSeverity.OK);
  }

  public long warnCount() {
    return countBySeverity(DoctorSeverity.WARN);
  }

  public long failCount() {
    return countBySeverity(DoctorSeverity.FAIL);
  }

  private long countBySeverity(DoctorSeverity severity) {
    return checks.stream().filter(result -> result.severity() == severity).count();
  }
}

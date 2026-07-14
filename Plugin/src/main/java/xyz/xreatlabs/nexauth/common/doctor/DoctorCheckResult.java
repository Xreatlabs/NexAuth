/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import java.util.Comparator;
import java.util.Objects;

public record DoctorCheckResult(
    String checkId, DoctorSeverity severity, String message, String detail, String remediation)
    implements Comparable<DoctorCheckResult> {

  private static final Comparator<DoctorCheckResult> ORDERING =
      Comparator.comparingInt((DoctorCheckResult result) -> result.severity.ordinal())
          .reversed()
          .thenComparing(DoctorCheckResult::checkId)
          .thenComparing(DoctorCheckResult::message);

  public DoctorCheckResult {
    Objects.requireNonNull(checkId, "checkId");
    Objects.requireNonNull(severity, "severity");
    Objects.requireNonNull(message, "message");
  }

  public DoctorCheckResult(String checkId, DoctorSeverity severity, String message) {
    this(checkId, severity, message, null, null);
  }

  @Override
  public int compareTo(DoctorCheckResult other) {
    return ORDERING.compare(this, other);
  }
}

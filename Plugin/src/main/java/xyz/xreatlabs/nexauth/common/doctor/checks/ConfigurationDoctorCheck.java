/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor.checks;

import java.util.Arrays;
import xyz.xreatlabs.nexauth.common.doctor.DoctorCheck;
import xyz.xreatlabs.nexauth.common.doctor.DoctorCheckResult;
import xyz.xreatlabs.nexauth.common.doctor.DoctorSeverity;
import xyz.xreatlabs.nexauth.common.reliability.FailurePolicyMode;

public record ConfigurationDoctorCheck(String rawFailurePolicyMode) implements DoctorCheck {

  @Override
  public DoctorCheckResult run() {
    if (rawFailurePolicyMode == null || rawFailurePolicyMode.isBlank()) {
      return new DoctorCheckResult(
          "configuration",
          DoctorSeverity.WARN,
          "Failure policy mode missing, using HARD_FAIL",
          "Resolved mode: HARD_FAIL",
          "Set failure-policy.mode to HARD_FAIL, RETRY_THEN_DISABLE, or DEGRADE");
    }

    var normalized = rawFailurePolicyMode.trim().toUpperCase();
    var recognized =
        Arrays.stream(FailurePolicyMode.values()).anyMatch(mode -> mode.name().equals(normalized));

    if (!recognized) {
      return new DoctorCheckResult(
          "configuration",
          DoctorSeverity.WARN,
          "Unknown failure policy mode '" + rawFailurePolicyMode + "', using HARD_FAIL",
          "Resolved mode: HARD_FAIL",
          "Set failure-policy.mode to HARD_FAIL, RETRY_THEN_DISABLE, or DEGRADE");
    }

    return new DoctorCheckResult(
        "configuration", DoctorSeverity.OK, "Failure policy mode set to " + normalized);
  }
}

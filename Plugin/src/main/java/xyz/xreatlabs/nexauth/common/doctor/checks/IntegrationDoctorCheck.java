/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor.checks;

import java.util.ArrayList;
import java.util.List;
import xyz.xreatlabs.nexauth.api.PlatformHandle;
import xyz.xreatlabs.nexauth.common.doctor.DoctorCheck;
import xyz.xreatlabs.nexauth.common.doctor.DoctorCheckResult;
import xyz.xreatlabs.nexauth.common.doctor.DoctorSeverity;

public record IntegrationDoctorCheck(
    boolean emailAvailable,
    boolean totpAvailable,
    boolean limboAvailable,
    PlatformHandle.ProxyData proxyData)
    implements DoctorCheck {

  @Override
  public DoctorCheckResult run() {
    if (proxyData == null) {
      return new DoctorCheckResult(
          "integration",
          DoctorSeverity.FAIL,
          "Proxy data unavailable",
          "Platform handle returned null proxy metadata",
          "Verify platform bootstrap completed before running diagnostics");
    }

    var unavailable = new ArrayList<String>();
    if (!emailAvailable) unavailable.add("Email");
    if (!totpAvailable) unavailable.add("TOTP");
    if (!limboAvailable) unavailable.add("limbo");

    if (!unavailable.isEmpty()) {
      return new DoctorCheckResult(
          "integration",
          DoctorSeverity.WARN,
          joinUnavailable(unavailable) + " integrations are unavailable",
          "Available proxy: " + proxyData.name(),
          "Install or configure the missing integrations if your deployment expects them");
    }

    return new DoctorCheckResult(
        "integration", DoctorSeverity.OK, "Core integrations and proxy data are available");
  }

  private static String joinUnavailable(List<String> unavailable) {
    if (unavailable.size() == 1) {
      return unavailable.getFirst();
    }
    if (unavailable.size() == 2) {
      return unavailable.getFirst() + " and " + unavailable.get(1);
    }
    return String.join(", ", unavailable.subList(0, unavailable.size() - 1))
        + ", and "
        + unavailable.getLast();
  }
}

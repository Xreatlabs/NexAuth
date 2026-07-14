/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import java.util.List;
import java.util.Objects;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.config.ConfigurationKeys;
import xyz.xreatlabs.nexauth.common.doctor.checks.ConfigurationDoctorCheck;
import xyz.xreatlabs.nexauth.common.doctor.checks.DatabaseDoctorCheck;
import xyz.xreatlabs.nexauth.common.doctor.checks.IntegrationDoctorCheck;

public record DoctorService(List<DoctorCheck> checks) {

  public DoctorService {
    Objects.requireNonNull(checks, "checks");
    checks = List.copyOf(checks);
  }

  public static DoctorService forPlugin(AuthenticNexAuth<?, ?> plugin) {
    var rawFailurePolicyMode =
        plugin.getConfiguration() == null
            ? null
            : plugin.getConfiguration().get(ConfigurationKeys.FAILURE_POLICY_MODE);

    return new DoctorService(
        List.of(
            new ConfigurationDoctorCheck(rawFailurePolicyMode),
            new DatabaseDoctorCheck(
                plugin.getDatabaseProvider(), plugin.getRuntimeDatabaseConnector()),
            new IntegrationDoctorCheck(
                plugin.getEmailHandler() != null,
                plugin.getTOTPProvider() != null,
                plugin.getLimboIntegration() != null,
                plugin.getPlatformHandle().getProxyData())));
  }

  public DoctorReport run() {
    return new DoctorReport(checks.stream().map(DoctorCheck::run).toList());
  }
}

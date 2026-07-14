/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor.checks;

import xyz.xreatlabs.nexauth.api.database.ReadWriteDatabaseProvider;
import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;
import xyz.xreatlabs.nexauth.common.doctor.DoctorCheck;
import xyz.xreatlabs.nexauth.common.doctor.DoctorCheckResult;
import xyz.xreatlabs.nexauth.common.doctor.DoctorSeverity;

public record DatabaseDoctorCheck(
    ReadWriteDatabaseProvider provider, DatabaseConnector<?, ?> connector) implements DoctorCheck {

  @Override
  public DoctorCheckResult run() {
    if (provider == null) {
      return new DoctorCheckResult(
          "database",
          DoctorSeverity.FAIL,
          "Database provider unavailable",
          "No active ReadWriteDatabaseProvider is registered",
          "Check database.type and connector initialization during startup");
    }
    if (connector == null) {
      return new DoctorCheckResult(
          "database",
          DoctorSeverity.WARN,
          "Database connector unavailable",
          "Read/write provider exists without an exposed runtime connector",
          "Verify the configured provider exposes a connector for health checks");
    }
    if (!connector.connected()) {
      return new DoctorCheckResult(
          "database",
          DoctorSeverity.FAIL,
          "Database connector is disconnected",
          "Observed connector state: disconnected",
          "Verify database credentials and restart NexAuth");
    }
    return new DoctorCheckResult(
        "database", DoctorSeverity.OK, "Database provider and connector are ready");
  }
}

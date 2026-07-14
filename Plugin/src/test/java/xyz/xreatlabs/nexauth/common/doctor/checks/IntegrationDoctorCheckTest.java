/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.PlatformHandle;
import xyz.xreatlabs.nexauth.common.doctor.DoctorSeverity;

class IntegrationDoctorCheckTest {

  @Test
  void failsWhenProxyDataIsUnavailable() {
    var result = new IntegrationDoctorCheck(true, true, true, null).run();

    assertEquals(DoctorSeverity.FAIL, result.severity());
    assertEquals("Proxy data unavailable", result.message());
  }

  @Test
  void warnsWhenOptionalIntegrationsAreUnavailable() {
    var result =
        new IntegrationDoctorCheck(
                false,
                false,
                false,
                new PlatformHandle.ProxyData("proxy", List.of(), List.of(), List.of(), List.of()))
            .run();

    assertEquals(DoctorSeverity.WARN, result.severity());
    assertEquals("Email, TOTP, and limbo integrations are unavailable", result.message());
  }

  @Test
  void reportsOkWhenIntegrationsAndProxyDataAreAvailable() {
    var result =
        new IntegrationDoctorCheck(
                true,
                true,
                true,
                new PlatformHandle.ProxyData("proxy", List.of(), List.of(), List.of(), List.of()))
            .run();

    assertEquals(DoctorSeverity.OK, result.severity());
    assertEquals("Core integrations and proxy data are available", result.message());
  }
}

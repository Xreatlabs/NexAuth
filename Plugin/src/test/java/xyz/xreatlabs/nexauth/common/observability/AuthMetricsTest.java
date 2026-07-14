/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.event.events.AuthenticatedEvent;
import xyz.xreatlabs.nexauth.api.event.events.WrongPasswordEvent;

class AuthMetricsTest {

  @Test
  void recordsCountersByType() {
    var metrics = new AuthMetrics();

    metrics.recordWrongPassword(WrongPasswordEvent.AuthenticationSource.LOGIN);
    metrics.recordWrongPassword(WrongPasswordEvent.AuthenticationSource.TOTP);
    metrics.recordAuthenticated(AuthenticatedEvent.AuthenticationReason.LOGIN);
    metrics.recordAuthenticated(AuthenticatedEvent.AuthenticationReason.REGISTER);

    var json = metrics.toJson();

    assertEquals(2, json.getAsJsonObject("wrong_password").get("total").getAsLong());
    assertEquals(1, json.getAsJsonObject("wrong_password").get("login").getAsLong());
    assertEquals(1, json.getAsJsonObject("wrong_password").get("totp").getAsLong());

    assertEquals(2, json.getAsJsonObject("authenticated").get("total").getAsLong());
    assertEquals(1, json.getAsJsonObject("authenticated").get("login").getAsLong());
    assertEquals(1, json.getAsJsonObject("authenticated").get("register").getAsLong());
  }
}

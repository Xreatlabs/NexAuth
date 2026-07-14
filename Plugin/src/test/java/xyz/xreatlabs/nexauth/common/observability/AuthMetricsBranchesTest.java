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

class AuthMetricsBranchesTest {

  @Test
  void recordsAllRemainingBranches() {
    var metrics = new AuthMetrics();

    metrics.recordWrongPassword(WrongPasswordEvent.AuthenticationSource.CHANGE_PASSWORD);
    metrics.recordWrongPassword(WrongPasswordEvent.AuthenticationSource.PREMIUM_ENABLE);
    metrics.recordWrongPassword(WrongPasswordEvent.AuthenticationSource.SET_EMAIL);
    metrics.recordAuthenticated(AuthenticatedEvent.AuthenticationReason.PREMIUM);
    metrics.recordAuthenticated(AuthenticatedEvent.AuthenticationReason.SESSION);

    var json = metrics.toJson();

    assertEquals(3, json.getAsJsonObject("wrong_password").get("total").getAsLong());
    assertEquals(1, json.getAsJsonObject("wrong_password").get("change_password").getAsLong());
    assertEquals(1, json.getAsJsonObject("wrong_password").get("premium_enable").getAsLong());
    assertEquals(1, json.getAsJsonObject("wrong_password").get("set_email").getAsLong());

    assertEquals(2, json.getAsJsonObject("authenticated").get("total").getAsLong());
    assertEquals(1, json.getAsJsonObject("authenticated").get("premium").getAsLong());
    assertEquals(1, json.getAsJsonObject("authenticated").get("session").getAsLong());
  }
}

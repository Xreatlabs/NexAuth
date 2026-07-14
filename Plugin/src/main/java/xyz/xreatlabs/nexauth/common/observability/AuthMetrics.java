/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.observability;

import com.google.gson.JsonObject;
import java.util.concurrent.atomic.AtomicLong;
import xyz.xreatlabs.nexauth.api.event.events.AuthenticatedEvent;
import xyz.xreatlabs.nexauth.api.event.events.WrongPasswordEvent;

public class AuthMetrics {

  private final AtomicLong wrongPasswordTotal = new AtomicLong();
  private final AtomicLong wrongPasswordLogin = new AtomicLong();
  private final AtomicLong wrongPasswordTotp = new AtomicLong();
  private final AtomicLong wrongPasswordChangePassword = new AtomicLong();
  private final AtomicLong wrongPasswordPremiumEnable = new AtomicLong();
  private final AtomicLong wrongPasswordSetEmail = new AtomicLong();

  private final AtomicLong authenticatedTotal = new AtomicLong();
  private final AtomicLong authenticatedLogin = new AtomicLong();
  private final AtomicLong authenticatedRegister = new AtomicLong();
  private final AtomicLong authenticatedPremium = new AtomicLong();
  private final AtomicLong authenticatedSession = new AtomicLong();

  public void recordWrongPassword(WrongPasswordEvent.AuthenticationSource source) {
    wrongPasswordTotal.incrementAndGet();

    switch (source) {
      case LOGIN -> wrongPasswordLogin.incrementAndGet();
      case TOTP -> wrongPasswordTotp.incrementAndGet();
      case CHANGE_PASSWORD -> wrongPasswordChangePassword.incrementAndGet();
      case PREMIUM_ENABLE -> wrongPasswordPremiumEnable.incrementAndGet();
      case SET_EMAIL -> wrongPasswordSetEmail.incrementAndGet();
    }
  }

  public void recordAuthenticated(AuthenticatedEvent.AuthenticationReason reason) {
    authenticatedTotal.incrementAndGet();

    switch (reason) {
      case LOGIN -> authenticatedLogin.incrementAndGet();
      case REGISTER -> authenticatedRegister.incrementAndGet();
      case PREMIUM -> authenticatedPremium.incrementAndGet();
      case SESSION -> authenticatedSession.incrementAndGet();
    }
  }

  public JsonObject toJson() {
    var root = new JsonObject();

    var wrong = new JsonObject();
    wrong.addProperty("total", wrongPasswordTotal.get());
    wrong.addProperty("login", wrongPasswordLogin.get());
    wrong.addProperty("totp", wrongPasswordTotp.get());
    wrong.addProperty("change_password", wrongPasswordChangePassword.get());
    wrong.addProperty("premium_enable", wrongPasswordPremiumEnable.get());
    wrong.addProperty("set_email", wrongPasswordSetEmail.get());

    var auth = new JsonObject();
    auth.addProperty("total", authenticatedTotal.get());
    auth.addProperty("login", authenticatedLogin.get());
    auth.addProperty("register", authenticatedRegister.get());
    auth.addProperty("premium", authenticatedPremium.get());
    auth.addProperty("session", authenticatedSession.get());

    root.add("wrong_password", wrong);
    root.add("authenticated", auth);

    return root;
  }
}

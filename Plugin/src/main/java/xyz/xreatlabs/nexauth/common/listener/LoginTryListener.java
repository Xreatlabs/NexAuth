/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import xyz.xreatlabs.nexauth.api.event.events.AuthenticatedEvent;
import xyz.xreatlabs.nexauth.api.event.events.WrongPasswordEvent;
import xyz.xreatlabs.nexauth.api.event.events.WrongPasswordEvent.AuthenticationSource;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.command.InvalidCommandArgument;
import xyz.xreatlabs.nexauth.common.config.ConfigurationKeys;

public class LoginTryListener<P, S> {

  private final AuthenticNexAuth<P, S> plugin;
  private final Cache<P, Integer> loginTries;
  private final Cache<P, Integer> securityFailures;
  private final Cache<P, Long> lockoutUntil;
  private final Cache<P, Long> backoffUntil;

  public LoginTryListener(AuthenticNexAuth<P, S> libreLogin) {
    this.plugin = libreLogin;
    this.loginTries =
        Caffeine.newBuilder()
            .expireAfterAccess(
                plugin
                    .getConfiguration()
                    .get(ConfigurationKeys.MILLISECONDS_TO_EXPIRE_LOGIN_ATTEMPTS),
                TimeUnit.MILLISECONDS)
            .build();
    this.securityFailures =
        Caffeine.newBuilder()
            .expireAfterAccess(
                plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_LOCKOUT_DURATION_MS),
                TimeUnit.MILLISECONDS)
            .build();
    this.lockoutUntil =
        Caffeine.newBuilder()
            .expireAfterAccess(
                plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_LOCKOUT_DURATION_MS),
                TimeUnit.MILLISECONDS)
            .build();
    this.backoffUntil =
        Caffeine.newBuilder()
            .expireAfterAccess(
                plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_BACKOFF_MAX_MS),
                TimeUnit.MILLISECONDS)
            .build();
    libreLogin
        .getEventProvider()
        .subscribe(libreLogin.getEventTypes().wrongPassword, this::onWrongPassword);
    libreLogin
        .getEventProvider()
        .subscribe(libreLogin.getEventTypes().authenticated, this::onAuthenticated);
  }

  private void onWrongPassword(WrongPasswordEvent<P, S> wrongPasswordEvent) {
    AuthenticationSource source = wrongPasswordEvent.getSource();
    if (source != AuthenticationSource.LOGIN && source != AuthenticationSource.TOTP) return;

    var player = wrongPasswordEvent.getPlayer();

    if (plugin.getConfiguration().get(ConfigurationKeys.MAX_LOGIN_ATTEMPTS) != -1) {
      int currentLoginTry = loginTries.asMap().merge(player, 1, Integer::sum);
      if (currentLoginTry >= plugin.getConfiguration().get(ConfigurationKeys.MAX_LOGIN_ATTEMPTS)) {
        String kickMessage =
            source == AuthenticationSource.LOGIN
                ? "kick-error-password-wrong"
                : "kick-error-totp-wrong";
        plugin.getPlatformHandle().kick(player, plugin.getMessages().getMessage(kickMessage));
      }
    }

    int securityFailureCount = securityFailures.asMap().merge(player, 1, Integer::sum);

    if (plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_LOCKOUT_ENABLED)) {
      var threshold =
          Math.max(
              1, plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_LOCKOUT_THRESHOLD));
      var duration =
          Math.max(
              1,
              plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_LOCKOUT_DURATION_MS));

      if (securityFailureCount >= threshold) {
        lockoutUntil.put(player, System.currentTimeMillis() + duration);
        String kickMessage =
            source == AuthenticationSource.LOGIN
                ? "kick-error-password-wrong"
                : "kick-error-totp-wrong";
        plugin.getPlatformHandle().kick(player, plugin.getMessages().getMessage(kickMessage));
      }
    }

    if (plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_BACKOFF_ENABLED)) {
      var initial =
          Math.max(
              1,
              plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_BACKOFF_INITIAL_MS));
      var max =
          Math.max(
              initial,
              plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_BACKOFF_MAX_MS));
      var multiplier =
          Math.max(
              1,
              plugin.getConfiguration().get(ConfigurationKeys.SECURITY_LOGIN_BACKOFF_MULTIPLIER));

      long delay = initial;
      for (int i = 1; i < securityFailureCount; i++) {
        delay = Math.min(max, delay * multiplier);
      }
      backoffUntil.put(player, System.currentTimeMillis() + delay);
    }
  }

  public void ensureCanAttempt(P player) {
    var now = System.currentTimeMillis();

    var lockout = lockoutUntil.getIfPresent(player);
    if (lockout != null && lockout > now) {
      throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-throttle"));
    }

    var backoff = backoffUntil.getIfPresent(player);
    if (backoff != null && backoff > now) {
      throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-throttle"));
    }
  }

  private void onAuthenticated(AuthenticatedEvent<P, S> authenticatedEvent) {
    var player = authenticatedEvent.getPlayer();
    loginTries.invalidate(player);
    securityFailures.invalidate(player);
    lockoutUntil.invalidate(player);
    backoffUntil.invalidate(player);
  }
}

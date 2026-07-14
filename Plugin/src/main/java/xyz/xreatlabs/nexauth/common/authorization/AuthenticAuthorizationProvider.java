/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.authorization;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import xyz.xreatlabs.nexauth.api.authorization.AuthorizationProvider;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.event.events.AuthenticatedEvent;
import xyz.xreatlabs.nexauth.api.totp.TOTPData;
import xyz.xreatlabs.nexauth.common.AuthenticHandler;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.config.ConfigurationKeys;
import xyz.xreatlabs.nexauth.common.event.events.AuthenticAuthenticatedEvent;

public class AuthenticAuthorizationProvider<P, S> extends AuthenticHandler<P, S>
    implements AuthorizationProvider<P> {

  private final Map<P, Boolean> unAuthorized;
  private final Map<P, String> awaiting2FA;
  private final Cache<UUID, EmailVerifyData> emailConfirmCache;
  private final Cache<UUID, String> passwordResetCache;
  private final Cache<UUID, AtomicInteger> totpAttemptCache;

  public AuthenticAuthorizationProvider(AuthenticNexAuth<P, S> plugin) {
    super(plugin);
    unAuthorized = new ConcurrentHashMap<>();
    awaiting2FA = new ConcurrentHashMap<>();

    var millis =
        plugin.getConfiguration().get(ConfigurationKeys.MILLISECONDS_TO_REFRESH_NOTIFICATION);

    if (millis > 0) {
      plugin.repeat(this::notifyUnauthorized, 0, millis);
    }

    plugin.repeat(this::broadcastActionbars, 0, 1000);

    emailConfirmCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    passwordResetCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    totpAttemptCache =
        Caffeine.newBuilder()
            .expireAfterWrite(
                Math.max(
                    1,
                    plugin
                        .getConfiguration()
                        .get(ConfigurationKeys.SECURITY_TOTP_ATTEMPT_WINDOW_MS)),
                TimeUnit.MILLISECONDS)
            .build();
  }

  public Cache<UUID, EmailVerifyData> getEmailConfirmCache() {
    return emailConfirmCache;
  }

  public Cache<UUID, String> getPasswordResetCache() {
    return passwordResetCache;
  }

  public void onExit(P player) {
    stopTracking(player);
    awaiting2FA.remove(player);
    var uuid = platformHandle.getUUIDForPlayer(player);
    emailConfirmCache.invalidate(uuid);
    passwordResetCache.invalidate(uuid);
    totpAttemptCache.invalidate(uuid);
  }

  @Override
  public boolean isAuthorized(P player) {
    return !unAuthorized.containsKey(player);
  }

  @Override
  public boolean isAwaiting2FA(P player) {
    return awaiting2FA.containsKey(player);
  }

  @Override
  public void authorize(User user, P player, AuthenticatedEvent.AuthenticationReason reason) {
    if (isAuthorized(player)) {
      throw new IllegalStateException("Player is already authorized");
    }
    stopTracking(player);

    user.setLastAuthentication(Timestamp.valueOf(LocalDateTime.now()));
    user.setIp(platformHandle.getIP(player));
    plugin.getDatabaseProvider().updateUser(user);

    var audience = platformHandle.getAudienceForPlayer(player);

    audience.clearTitle();
    audience.sendActionBar(Component.empty());
    plugin
        .getEventProvider()
        .fire(
            plugin.getEventTypes().authenticated,
            new AuthenticAuthenticatedEvent<>(user, player, plugin, reason));
    plugin.authorize(player, user, audience);
  }

  @Override
  public boolean confirmTwoFactorAuth(P player, Integer code, User user) {
    var secret = awaiting2FA.get(player);
    var uuid = platformHandle.getUUIDForPlayer(player);
    var totpProvider = plugin.getTOTPProvider();

    if (secret == null || totpProvider == null) {
      return false;
    }

    if (plugin.getConfiguration().get(ConfigurationKeys.SECURITY_TOTP_ATTEMPT_LIMIT_ENABLED)) {
      var maxAttempts =
          Math.max(1, plugin.getConfiguration().get(ConfigurationKeys.SECURITY_TOTP_MAX_ATTEMPTS));
      var attempts = totpAttemptCache.get(uuid, ignored -> new AtomicInteger(0));

      if (attempts.get() >= maxAttempts) {
        return false;
      }

      if (!totpProvider.verify(code, secret)) {
        attempts.incrementAndGet();
        return false;
      }
    } else if (!totpProvider.verify(code, secret)) {
      return false;
    }

    user.setSecret(secret);
    plugin.getDatabaseProvider().updateUser(user);
    totpAttemptCache.invalidate(uuid);
    return true;
  }

  public void startTracking(User user, P player) {
    var audience = platformHandle.getAudienceForPlayer(player);

    unAuthorized.put(player, user.isRegistered());

    plugin.cancelOnExit(
        plugin.delay(
            () -> {
              if (!unAuthorized.containsKey(player)) return;
              sendInfoMessage(user.isRegistered(), audience);
            },
            250),
        player);

    var limit = plugin.getConfiguration().get(ConfigurationKeys.SECONDS_TO_AUTHORIZE);

    if (limit > 0) {
      plugin.cancelOnExit(
          plugin.delay(
              () -> {
                if (!unAuthorized.containsKey(player)) return;
                platformHandle.kick(player, plugin.getMessages().getMessage("kick-time-limit"));
              },
              limit * 1000L),
          player);
    }

    sendInfoMessage(user.isRegistered(), audience);
  }

  private void broadcastActionbars() {
    var wrong = new HashSet<P>();
    unAuthorized.forEach(
        (player, registered) -> {
          var audience = platformHandle.getAudienceForPlayer(player);

          if (audience == null) {
            wrong.add(player);
            return;
          }

          sendActionBar(registered, audience);
        });

    wrong.forEach(unAuthorized::remove);
  }

  private void sendActionBar(boolean registered, Audience audience) {
    if (plugin.getConfiguration().get(ConfigurationKeys.USE_ACTION_BAR)) {
      audience.sendActionBar(
          plugin.getMessages().getMessage(registered ? "action-bar-login" : "action-bar-register"));
    }
  }

  private void sendInfoMessage(boolean registered, Audience audience) {
    audience.sendMessage(
        plugin.getMessages().getMessage(registered ? "prompt-login" : "prompt-register"));
    if (!plugin.getConfiguration().get(ConfigurationKeys.USE_TITLES)) return;
    var toRefresh =
        plugin.getConfiguration().get(ConfigurationKeys.MILLISECONDS_TO_REFRESH_NOTIFICATION);
    //noinspection UnstableApiUsage
    audience.showTitle(
        Title.title(
            plugin.getMessages().getMessage(registered ? "title-login" : "title-register"),
            plugin.getMessages().getMessage(registered ? "sub-title-login" : "sub-title-register"),
            Title.Times.times(
                Duration.ofMillis(0),
                Duration.ofMillis(toRefresh > 0 ? (long) (toRefresh * 1.1) : 10000),
                Duration.ofMillis(0))));
  }

  public void stopTracking(P player) {
    unAuthorized.remove(player);
  }

  public void notifyUnauthorized() {
    var wrong = new HashSet<P>();
    unAuthorized.forEach(
        (player, registered) -> {
          var audience = platformHandle.getAudienceForPlayer(player);

          if (audience == null) {
            wrong.add(player);
            return;
          }

          sendInfoMessage(registered, audience);
        });

    wrong.forEach(unAuthorized::remove);
  }

  public boolean hasExceededTotpAttempts(P player) {
    if (!plugin.getConfiguration().get(ConfigurationKeys.SECURITY_TOTP_ATTEMPT_LIMIT_ENABLED)) {
      return false;
    }

    var uuid = platformHandle.getUUIDForPlayer(player);
    var attempts = totpAttemptCache.getIfPresent(uuid);
    if (attempts == null) {
      return false;
    }

    var maxAttempts =
        Math.max(1, plugin.getConfiguration().get(ConfigurationKeys.SECURITY_TOTP_MAX_ATTEMPTS));
    return attempts.get() >= maxAttempts;
  }

  public record EmailVerifyData(String email, String token, UUID uuid) {}

  public void beginTwoFactorAuth(User user, P player, TOTPData data) {
    awaiting2FA.put(player, data.secret());

    var limbo = plugin.getServerHandler().chooseLimboServer(user, player);

    if (limbo == null) {
      platformHandle.kick(player, plugin.getMessages().getMessage("kick-no-limbo"));
      return;
    }

    platformHandle
        .movePlayer(player, limbo)
        .whenComplete(
            (t, e) -> {
              if (t != null || e != null) awaiting2FA.remove(player);
            });
  }
}

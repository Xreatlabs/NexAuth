/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.database.ReadDatabaseProvider;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.util.Release;
import xyz.xreatlabs.nexauth.api.util.SemanticVersion;

class AuthRuntimeGuardsTest {

  @Test
  void validReleasesDropsNullAndInvalidEntries() {
    var valid = new Release(new SemanticVersion(0, 0, 2, false), "0.0.2");

    var releases =
        AuthRuntimeGuards.validReleases(Arrays.asList(null, new Release(null, "broken"), valid));

    assertEquals(List.of(valid), releases);
  }

  @Test
  void resolveLatestVersionFallsBackToFirstValidRelease() {
    var latest =
        AuthRuntimeGuards.resolveLatestVersion(
            null, List.of(new Release(new SemanticVersion(0, 0, 2, false), "0.0.2")));

    assertEquals(new SemanticVersion(0, 0, 2, false), latest);
  }

  @Test
  void resolveUserFallsBackToNameWhenUuidLookupMisses() {
    var expected = user("VelocityPlayer");
    var provider = provider(null, expected);

    var resolved = AuthRuntimeGuards.resolveUser(provider, UUID.randomUUID(), "VelocityPlayer");

    assertSame(expected, resolved);
  }

  @Test
  void resolveUserReturnsNullWhenBothLookupsMiss() {
    var provider = provider(null, null);

    assertNull(AuthRuntimeGuards.resolveUser(provider, UUID.randomUUID(), "MissingPlayer"));
  }

  private static ReadDatabaseProvider provider(User byUuid, User byName) {
    return (ReadDatabaseProvider)
        Proxy.newProxyInstance(
            ReadDatabaseProvider.class.getClassLoader(),
            new Class[] {ReadDatabaseProvider.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "getByUUID" -> byUuid;
                  case "getByName" -> byName;
                  case "getByPremiumUUID" -> null;
                  case "getAllUsers", "getByIP" -> List.of();
                  default -> throw new UnsupportedOperationException(method.getName());
                });
  }

  private static User user(String name) {
    return (User)
        Proxy.newProxyInstance(
            User.class.getClassLoader(),
            new Class[] {User.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "getLastNickname" -> name;
                  case "getUuid" -> UUID.nameUUIDFromBytes(name.getBytes());
                  case "getJoinDate", "getLastSeen" -> Timestamp.valueOf("2026-03-15 00:00:00");
                  case "isRegistered", "autoLoginEnabled" -> false;
                  case "getSecret",
                          "getIp",
                          "getLastServer",
                          "getLastAuthentication",
                          "getHashedPassword",
                          "getPremiumUUID",
                          "getEmail" ->
                      null;
                  case "setSecret",
                          "setIp",
                          "setLastServer",
                          "setLastAuthentication",
                          "setJoinDate",
                          "setLastSeen",
                          "setHashedPassword",
                          "setPremiumUUID",
                          "setLastNickname",
                          "setEmail" ->
                      null;
                  default -> throw new UnsupportedOperationException(method.getName());
                });
  }
}

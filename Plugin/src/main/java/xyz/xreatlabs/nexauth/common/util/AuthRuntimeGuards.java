/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import xyz.xreatlabs.nexauth.api.database.ReadDatabaseProvider;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.util.Release;
import xyz.xreatlabs.nexauth.api.util.SemanticVersion;

public final class AuthRuntimeGuards {

  private AuthRuntimeGuards() {}

  public static List<Release> validReleases(@Nullable Collection<Release> releases) {
    if (releases == null) {
      return List.of();
    }

    return releases.stream()
        .filter(Objects::nonNull)
        .filter(release -> release.version() != null)
        .toList();
  }

  @Nullable
  public static SemanticVersion resolveLatestVersion(
      @Nullable SemanticVersion latest, Collection<Release> releases) {
    if (latest != null) {
      return latest;
    }

    if (releases == null || releases.isEmpty()) {
      return null;
    }

    return releases.iterator().next().version();
  }

  @Nullable
  public static User resolveUser(
      @Nullable ReadDatabaseProvider databaseProvider,
      @Nullable UUID uuid,
      @Nullable String username) {
    if (databaseProvider == null) {
      return null;
    }

    User user = uuid == null ? null : databaseProvider.getByUUID(uuid);

    if (user == null && username != null && !username.isBlank()) {
      user = databaseProvider.getByName(username);
    }

    return user;
  }
}

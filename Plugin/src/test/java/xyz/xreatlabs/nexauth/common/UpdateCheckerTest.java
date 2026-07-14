/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.util.Release;
import xyz.xreatlabs.nexauth.api.util.SemanticVersion;

class UpdateCheckerTest {

  private static final Gson GSON = new Gson();

  @Test
  void githubApiReturnsReleasesIncludingPreReleases() throws Exception {
    var connection =
        new URL("https://api.github.com/repos/Xreatlabs/NexAuth/releases").openConnection();
    connection.setRequestProperty("User-Agent", "NexAuth/test");
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(10000);

    try (var in = connection.getInputStream()) {
      var root = GSON.fromJson(new InputStreamReader(in), JsonArray.class);
      assertNotNull(root);
      assertTrue(root.size() > 0, "GitHub API should return at least one release");

      List<Release> releases = new ArrayList<>();
      SemanticVersion latest = null;

      for (var raw : root) {
        var release = raw.getAsJsonObject();

        if (release.get("draft") != null && release.get("draft").getAsBoolean()) {
          continue;
        }

        var versionElement = release.get("tag_name");
        var version =
            versionElement == null ? null : SemanticVersion.parse(versionElement.getAsString());
        if (version == null) {
          continue;
        }

        var nameElement = release.get("name");
        var name = nameElement == null ? "Unknown release" : nameElement.getAsString();

        releases.add(new Release(version, name));
        if (latest == null) {
          latest = version;
        }
      }

      assertNotNull(latest, "Should find at least one valid release (including pre-releases)");
      assertFalse(releases.isEmpty(), "Release list should not be empty");

      // Verify the known pre-release is present
      boolean hasBeta4 =
          releases.stream().anyMatch(r -> r.version().toString().equals("0.0.1-dev"));
      assertTrue(hasBeta4, "Should include 0.0.1-beta4 pre-release");
    }
  }

  @Test
  void githubRssFeedReturnsReleases() throws Exception {
    var connection = new URL("https://github.com/Xreatlabs/NexAuth/releases.atom").openConnection();
    connection.setRequestProperty("User-Agent", "NexAuth/test");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(10000);

    try (var in = connection.getInputStream()) {
      var content = new String(in.readAllBytes());
      var pattern = Pattern.compile("<title>([^<]+)</title>");
      var matcher = pattern.matcher(content);

      List<Release> releases = new ArrayList<>();
      SemanticVersion latest = null;

      while (matcher.find()) {
        var title = matcher.group(1);
        if (title.contains("NexAuth") && !title.equals("Release notes from NexAuth")) {
          var versionPattern = Pattern.compile("v?([0-9]+\\.[0-9]+\\.[0-9]+(?:-[a-zA-Z0-9]+)?)");
          var versionMatcher = versionPattern.matcher(title);

          if (versionMatcher.find()) {
            var version = SemanticVersion.parse(versionMatcher.group(1));
            if (version == null) {
              continue;
            }
            releases.add(new Release(version, title));
            if (latest == null) {
              latest = version;
            }
          }
        }
      }

      assertNotNull(latest, "RSS feed should contain at least one valid release");
      assertFalse(releases.isEmpty(), "RSS releases list should not be empty");
    }
  }

  @Test
  void releasesPageFallbackIgnoresTagsThatAreNotReleases() {
    var html =
        """
        <a href="/XreatLabs/NexAuth/tree/v1.0.0">historical tag</a>
        <a href="/XreatLabs/NexAuth/releases/tag/0.0.1-beta4">current release</a>
        """;

    var updateInfo = AuthenticNexAuth.parseGitHubReleasesPage(html);

    assertNotNull(updateInfo);
    assertEquals(SemanticVersion.parse("0.0.1-beta4"), updateInfo.latest());
    assertEquals(1, updateInfo.allReleases().size());
    assertEquals("NexAuth 0.0.1-beta4", updateInfo.allReleases().getFirst().name());
  }

  @Test
  void semanticVersionParsingHandlesBetaTags() {
    var v1 = SemanticVersion.parse("0.0.1-beta4");
    assertNotNull(v1);
    assertEquals(0, v1.major());
    assertEquals(0, v1.minor());
    assertEquals(1, v1.patch());
    assertTrue(v1.dev());

    var v2 = SemanticVersion.parse("v1.2.3");
    assertNotNull(v2);
    assertEquals(1, v2.major());
    assertEquals(2, v2.minor());
    assertEquals(3, v2.patch());
    assertFalse(v2.dev());

    var v4 = SemanticVersion.parse("v1.0.0-rc1");
    assertNotNull(v4);
    assertEquals(1, v4.major());
    assertTrue(v4.dev());

    var v3 = SemanticVersion.parse("1.0.0-SNAPSHOT");
    assertNotNull(v3);
    assertTrue(v3.dev());
  }
}

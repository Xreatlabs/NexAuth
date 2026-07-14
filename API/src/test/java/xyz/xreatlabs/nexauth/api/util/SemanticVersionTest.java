/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SemanticVersionTest {

  @Test
  void parseValidStableVersion() {
    var version = SemanticVersion.parse("1.0.0");

    assertNotNull(version);
    assertEquals(1, version.major());
    assertEquals(0, version.minor());
    assertEquals(0, version.patch());
    assertFalse(version.dev());
  }

  @Test
  void parseInvalidVersionReturnsNull() {
    assertNull(SemanticVersion.parse("not-a-version"));
    assertNull(SemanticVersion.parse("1.2"));
    assertNull(SemanticVersion.parse(null));
  }

  @Test
  void compareTreatsNullAsUnknownNewerVersion() {
    var current = SemanticVersion.parse("1.0.0");

    assertNotNull(current);
    assertEquals(1, current.compare(null));
  }
}

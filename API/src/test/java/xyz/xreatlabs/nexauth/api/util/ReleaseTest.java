/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReleaseTest {

  @Test
  void storesVersionAndName() {
    var version = new SemanticVersion(1, 2, 3, false);
    var release = new Release(version, "NexAuth 1.2.3");

    assertEquals(version, release.version());
    assertEquals("NexAuth 1.2.3", release.name());
    assertEquals(new Release(version, "NexAuth 1.2.3"), release);
  }
}

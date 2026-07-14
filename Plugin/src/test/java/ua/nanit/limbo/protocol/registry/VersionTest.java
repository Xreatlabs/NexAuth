/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  void resolvesKnownAndUnknownProtocolNumbers() {
    assertSame(Version.V1_21, Version.of(767));
    assertTrue(Version.of(775).isSupported());
    assertSame(Version.UNDEFINED, Version.of(Integer.MAX_VALUE));
  }

  @Test
  void comparesProtocolRangesCorrectly() {
    assertTrue(Version.V1_21.more(Version.V1_20_5));
    assertTrue(Version.V1_20_5.lessOrEqual(Version.V1_21));
    assertTrue(Version.V1_21.fromTo(Version.V1_20_5, Version.V1_21_7));
    assertFalse(Version.UNDEFINED.isSupported());
    assertEquals(Version.V1_7_2, Version.getMin());
    assertEquals("26.1", Version.getMax().getDisplayName());
  }
}

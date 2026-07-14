/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

class InfoForwardingTest {

  @Test
  void reportsTypeAndTokens() throws Exception {
    var forwarding = new InfoForwarding();
    set(forwarding, "type", InfoForwarding.Type.BUNGEE_GUARD);
    set(forwarding, "tokens", List.of("alpha", "beta"));

    assertTrue(forwarding.isBungeeGuard());
    assertFalse(forwarding.isModern());
    assertTrue(forwarding.hasToken("alpha"));
    assertFalse(forwarding.hasToken("missing"));
  }

  @Test
  void noneTypeReportsNone() throws Exception {
    var forwarding = new InfoForwarding();
    set(forwarding, "type", InfoForwarding.Type.NONE);

    assertTrue(forwarding.isNone());
    assertFalse(forwarding.isLegacy());
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}

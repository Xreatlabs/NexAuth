/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.common.config.ConfigurateHelper;

class ConfigurationKeyTest {

  @Test
  void computeFallsBackToDefaultWhenGetterReturnsNull() {
    var key = new ConfigurationKey<>("test.key", "fallback", "comment", (helper, path) -> null);

    assertEquals("fallback", key.compute((ConfigurateHelper) null));
  }

  @Test
  void settersUpdateCommentAndDefault() {
    var key = new ConfigurationKey<>("test.key", "fallback", "comment", (helper, path) -> "value");

    key.setComment("new comment");
    key.setDefault("new default");

    assertEquals("new comment", key.comment());
    assertEquals("new default", key.defaultValue());
  }

  @Test
  void getCommentKeyIsCommentOnly() {
    var key = ConfigurationKey.getComment("section", "hello");

    assertEquals("section", key.key());
    assertEquals("hello", key.comment());
    assertNull(key.defaultValue());
    assertThrows(UnsupportedOperationException.class, () -> key.getter().apply(null, "section"));
  }
}

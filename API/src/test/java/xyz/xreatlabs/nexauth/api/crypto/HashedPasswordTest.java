/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HashedPasswordTest {

  @Test
  void toStringIncludesKeyFields() {
    var password = new HashedPassword("abc", "salt", "argon2id");

    var rendered = password.toString();

    assertTrue(rendered.contains("hash='abc'"));
    assertTrue(rendered.contains("salt='salt'"));
    assertTrue(rendered.contains("algo='argon2id'"));
    assertEquals(new HashedPassword("abc", "salt", "argon2id"), password);
  }
}

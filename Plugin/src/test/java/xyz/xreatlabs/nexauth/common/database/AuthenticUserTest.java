/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.crypto.HashedPassword;

class AuthenticUserTest {

  @Test
  void registrationAndAutoLoginReflectStoredFields() {
    var user =
        new AuthenticUser(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new HashedPassword("hash", "salt", "algo"),
            "Kartvya69",
            Timestamp.valueOf("2026-03-15 00:00:00"),
            Timestamp.valueOf("2026-03-15 00:00:00"),
            "secret",
            "127.0.0.1",
            null,
            "lobby",
            "a@example.com");

    assertTrue(user.isRegistered());
    assertTrue(user.autoLoginEnabled());
    assertEquals("secret", user.getSecret());
    assertEquals("127.0.0.1", user.getIp());
    assertEquals("lobby", user.getLastServer());
    assertEquals("a@example.com", user.getEmail());
  }

  @Test
  void equalityAndHashCodeUseUuid() {
    var uuid = UUID.randomUUID();
    var first =
        new AuthenticUser(
            uuid,
            null,
            null,
            "One",
            new Timestamp(1),
            new Timestamp(2),
            null,
            null,
            null,
            null,
            null);
    var second =
        new AuthenticUser(
            uuid,
            UUID.randomUUID(),
            new HashedPassword("h", null, "a"),
            "Two",
            new Timestamp(3),
            new Timestamp(4),
            "s",
            "ip",
            new Timestamp(5),
            "srv",
            "mail");

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }
}

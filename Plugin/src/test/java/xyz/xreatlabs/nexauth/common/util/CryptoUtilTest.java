/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bouncycastle.crypto.params.Argon2Parameters;
import org.junit.jupiter.api.Test;

class CryptoUtilTest {

  @Test
  void convertsBcryptRawIntoStructuredPassword() {
    var password =
        CryptoUtil.convertFromBCryptRaw(
            "$2a$12$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXY1234567890");

    assertEquals("BCrypt-2A", password.algo());
    assertEquals("abcdefghijklmnopqrstuu", password.salt());
    assertEquals("12$ABCDEFGHIJKLMNOPQRSTUVWXY1234567890", password.hash());
    assertEquals(
        "$2a$12$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXY1234567890",
        CryptoUtil.rawBcryptFromHashed(password));
  }

  @Test
  void convertsArgon2HashesRoundTrip() {
    var salt = new byte[] {1, 2, 3, 4};
    var hash = new byte[] {5, 6, 7, 8};
    var parameters =
        new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(19)
            .withIterations(3)
            .withMemoryAsKB(64)
            .withSalt(salt)
            .build();

    var password = CryptoUtil.convertFromArgon2ID(hash, parameters);
    var raw = CryptoUtil.rawArgonFromHashed(password);

    assertEquals("Argon-2ID", password.algo());
    assertArrayEquals(hash, raw.hash());
    assertArrayEquals(salt, raw.parameters().getSalt());
    assertEquals(19, raw.parameters().getVersion());
    assertEquals(3, raw.parameters().getIterations());
    assertEquals(64, raw.parameters().getMemory());
  }
}

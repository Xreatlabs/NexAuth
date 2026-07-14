/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.totp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class TOTPDataTest {

  @Test
  void storesQrAndSecret() {
    var qr = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    var data = new TOTPData(qr, "SECRET-123");

    assertSame(qr, data.qr());
    assertEquals("SECRET-123", data.secret());
  }
}

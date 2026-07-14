/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

  @Test
  void limitsRepeatedAccessUntilWindowExpires() throws InterruptedException {
    var limiter = new RateLimiter<String>(50, TimeUnit.MILLISECONDS);

    assertFalse(limiter.tryAndLimit("player"));
    assertTrue(limiter.tryAndLimit("player"));

    Thread.sleep(80);

    assertFalse(limiter.tryAndLimit("player"));
  }

  @Test
  void limitsPerKeyIndependently() {
    var limiter = new RateLimiter<String>(1, TimeUnit.MINUTES);

    assertFalse(limiter.tryAndLimit("alpha"));
    assertFalse(limiter.tryAndLimit("beta"));
    assertTrue(limiter.tryAndLimit("alpha"));
    assertTrue(limiter.tryAndLimit("beta"));
  }
}

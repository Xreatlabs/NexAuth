/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.premium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class PremiumExceptionTest {

  @Test
  void storesIssueAndCause() {
    var cause = new IllegalStateException("rate limited");
    var exception = new PremiumException(PremiumException.Issue.THROTTLED, cause);

    assertEquals(PremiumException.Issue.THROTTLED, exception.getIssue());
    assertSame(cause, exception.getCause());
  }

  @Test
  void storesIssueAndMessage() {
    var exception =
        new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, "broken response");

    assertEquals(PremiumException.Issue.SERVER_EXCEPTION, exception.getIssue());
    assertEquals("broken response", exception.getMessage());
  }
}

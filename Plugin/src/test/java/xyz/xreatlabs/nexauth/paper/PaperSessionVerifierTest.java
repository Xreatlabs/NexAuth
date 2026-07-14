/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.paper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class PaperSessionVerifierTest {

  @Test
  void onlyAcceptsExplicitJoinedResponses() throws IOException {
    assertTrue(PaperSessionVerifier.isJoinedResponse(200));
    assertFalse(PaperSessionVerifier.isJoinedResponse(204));
  }

  @Test
  void failsClosedOnSessionServerErrors() {
    assertThrows(IOException.class, () -> PaperSessionVerifier.isJoinedResponse(403));
    assertThrows(IOException.class, () -> PaperSessionVerifier.isJoinedResponse(429));
    assertThrows(IOException.class, () -> PaperSessionVerifier.isJoinedResponse(500));
  }
}

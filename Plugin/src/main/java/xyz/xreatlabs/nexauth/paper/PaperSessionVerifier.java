/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.paper;

import java.io.IOException;

final class PaperSessionVerifier {

  private PaperSessionVerifier() {}

  static boolean isJoinedResponse(int responseCode) throws IOException {
    return switch (responseCode) {
      case 200 -> true;
      case 204 -> false;
      default -> throw new IOException("Unexpected Mojang session response: HTTP " + responseCode);
    };
  }
}

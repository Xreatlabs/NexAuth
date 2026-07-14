/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MessageKeySmokeTest {

  @Test
  void definesDoctorAndStatusMessageKeys() throws IOException {
    var source =
        Files.readString(
            Path.of("src/main/java/xyz/xreatlabs/nexauth/common/config/MessageKeys.java"));

    assertTrue(source.contains("INFO_STATUS_HEADER"));
    assertTrue(source.contains("INFO_STATUS_ENTRY"));
    assertTrue(source.contains("INFO_STATUS_METRICS"));
    assertTrue(source.contains("INFO_DOCTOR_HEADER"));
    assertTrue(source.contains("INFO_DOCTOR_ENTRY"));
    assertTrue(source.contains("INFO_DOCTOR_DETAIL"));
    assertTrue(source.contains("INFO_DOCTOR_SUMMARY"));
    assertTrue(source.contains("SYNTAX_STATUS"));
    assertTrue(source.contains("SYNTAX_DOCTOR"));
    assertTrue(source.contains("AUTOCOMPLETE_STATUS"));
    assertTrue(source.contains("AUTOCOMPLETE_DOCTOR"));
  }
}

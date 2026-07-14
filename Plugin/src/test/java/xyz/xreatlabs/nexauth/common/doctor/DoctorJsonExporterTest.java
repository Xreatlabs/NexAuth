/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.PlatformHandle;

class DoctorJsonExporterTest {

  @Test
  void exportsStatusAndDoctorSections() {
    var metrics = new JsonObject();
    metrics.addProperty("total", 4);
    var status =
        new StatusSnapshot(
            "1.0.0",
            "velocity",
            "DEGRADE",
            true,
            true,
            false,
            true,
            new PlatformHandle.ProxyData(
                "proxy-1",
                List.of("survival"),
                List.of("NexAuth"),
                List.of("limbo"),
                List.of("lobby")),
            metrics);
    var doctor =
        new DoctorReport(
            List.of(
                new DoctorCheckResult(
                    "database",
                    DoctorSeverity.FAIL,
                    "Database connector is disconnected",
                    "Observed connector state: disconnected",
                    "Verify database credentials and restart NexAuth"),
                new DoctorCheckResult(
                    "configuration",
                    DoctorSeverity.WARN,
                    "Unknown failure policy mode 'mystery-mode', using HARD_FAIL")));

    var json = DoctorJsonExporter.export(status, doctor);

    assertEquals("1.0.0", json.getAsJsonObject("status").get("version").getAsString());
    assertEquals("DEGRADE", json.getAsJsonObject("status").get("failurePolicyMode").getAsString());
    assertEquals(
        4, json.getAsJsonObject("status").getAsJsonObject("metrics").get("total").getAsLong());

    var doctorJson = json.getAsJsonObject("doctor");
    assertEquals("FAIL", doctorJson.get("severity").getAsString());
    assertEquals(0, doctorJson.get("ok").getAsLong());
    assertEquals(1, doctorJson.get("warn").getAsLong());
    assertEquals(1, doctorJson.get("fail").getAsLong());
    assertEquals(2, doctorJson.getAsJsonArray("checks").size());
    assertEquals(
        "Verify database credentials and restart NexAuth",
        doctorJson
            .getAsJsonArray("checks")
            .get(0)
            .getAsJsonObject()
            .get("remediation")
            .getAsString());
  }
}

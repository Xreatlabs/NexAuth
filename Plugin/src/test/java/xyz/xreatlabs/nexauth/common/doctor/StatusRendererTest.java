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

class StatusRendererTest {

  @Test
  void rendersCompactStatusSummaryLines() {
    var metrics = new JsonObject();
    metrics.addProperty("total", 7);
    var proxyData =
        new PlatformHandle.ProxyData(
            "proxy-1",
            List.of("survival", "lobby"),
            List.of("NexAuth"),
            List.of("limbo"),
            List.of("lobby"));
    var snapshot =
        new StatusSnapshot(
            "1.0.0", "velocity", "DEGRADE", true, true, false, true, proxyData, metrics);

    var rendered = StatusRenderer.render(snapshot);

    assertEquals("info-status-header", rendered.getFirst().messageKey());
    assertEquals(
        List.of("%version%", "1.0.0", "%platform%", "velocity"),
        rendered.getFirst().replacements());
    assertEquals(
        List.of(
            new RenderedLine(
                "info-status-entry", "%label%", "Failure policy", "%value%", "DEGRADE"),
            new RenderedLine("info-status-entry", "%label%", "Multi-proxy", "%value%", "Enabled"),
            new RenderedLine("info-status-entry", "%label%", "Email", "%value%", "Enabled"),
            new RenderedLine("info-status-entry", "%label%", "TOTP", "%value%", "Disabled"),
            new RenderedLine(
                "info-status-entry", "%label%", "Limbo integration", "%value%", "Available"),
            new RenderedLine("info-status-entry", "%label%", "Proxy", "%value%", "proxy-1"),
            new RenderedLine("info-status-metrics", "%json%", metrics.toString())),
        rendered.subList(1, rendered.size()));
  }
}

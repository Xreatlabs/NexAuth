/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import java.util.ArrayList;
import java.util.List;

public final class StatusRenderer {

  private StatusRenderer() {}

  public static List<RenderedLine> render(StatusSnapshot snapshot) {
    var lines = new ArrayList<RenderedLine>();
    lines.add(
        new RenderedLine(
            "info-status-header",
            "%version%",
            snapshot.version(),
            "%platform%",
            snapshot.platform()));
    lines.add(
        new RenderedLine(
            "info-status-entry",
            "%label%",
            "Failure policy",
            "%value%",
            snapshot.failurePolicyMode()));
    lines.add(
        new RenderedLine(
            "info-status-entry",
            "%label%",
            "Multi-proxy",
            "%value%",
            enabled(snapshot.multiProxyEnabled())));
    lines.add(
        new RenderedLine(
            "info-status-entry", "%label%", "Email", "%value%", enabled(snapshot.emailEnabled())));
    lines.add(
        new RenderedLine(
            "info-status-entry", "%label%", "TOTP", "%value%", enabled(snapshot.totpEnabled())));
    lines.add(
        new RenderedLine(
            "info-status-entry",
            "%label%",
            "Limbo integration",
            "%value%",
            available(snapshot.limboIntegrationAvailable())));
    if (snapshot.proxyData() != null) {
      lines.add(
          new RenderedLine(
              "info-status-entry", "%label%", "Proxy", "%value%", snapshot.proxyData().name()));
    }
    if (snapshot.metrics() != null) {
      lines.add(new RenderedLine("info-status-metrics", "%json%", snapshot.metrics().toString()));
    }
    return List.copyOf(lines);
  }

  private static String enabled(boolean value) {
    return value ? "Enabled" : "Disabled";
  }

  private static String available(boolean value) {
    return value ? "Available" : "Unavailable";
  }
}

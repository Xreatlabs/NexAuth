/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import xyz.xreatlabs.nexauth.api.PlatformHandle;

public final class DoctorJsonExporter {

  private static final Gson GSON = new Gson();

  private DoctorJsonExporter() {}

  public static JsonObject export(StatusSnapshot status, DoctorReport doctor) {
    var root = new JsonObject();
    root.add("status", exportStatus(status));
    root.add("doctor", exportDoctor(doctor));
    return root;
  }

  public static JsonObject exportStatus(StatusSnapshot status) {
    var json = new JsonObject();
    json.addProperty("version", status.version());
    json.addProperty("platform", status.platform());
    json.addProperty("failurePolicyMode", status.failurePolicyMode());
    json.addProperty("multiProxyEnabled", status.multiProxyEnabled());
    json.addProperty("emailEnabled", status.emailEnabled());
    json.addProperty("totpEnabled", status.totpEnabled());
    json.addProperty("limboIntegrationAvailable", status.limboIntegrationAvailable());
    if (status.proxyData() != null) {
      json.add("proxyData", exportProxyData(status.proxyData()));
    }
    if (status.metrics() != null) {
      json.add("metrics", status.metrics().deepCopy());
    }
    return json;
  }

  public static JsonObject exportDoctor(DoctorReport doctor) {
    var json = new JsonObject();
    json.addProperty("severity", doctor.overallSeverity().name());
    json.addProperty("ok", doctor.okCount());
    json.addProperty("warn", doctor.warnCount());
    json.addProperty("fail", doctor.failCount());

    var checks = new JsonArray();
    for (DoctorCheckResult check : doctor.checks()) {
      var item = new JsonObject();
      item.addProperty("checkId", check.checkId());
      item.addProperty("severity", check.severity().name());
      item.addProperty("message", check.message());
      if (check.detail() != null) {
        item.addProperty("detail", check.detail());
      }
      if (check.remediation() != null) {
        item.addProperty("remediation", check.remediation());
      }
      checks.add(item);
    }
    json.add("checks", checks);
    return json;
  }

  private static JsonObject exportProxyData(PlatformHandle.ProxyData proxyData) {
    var json = new JsonObject();
    json.addProperty("name", proxyData.name());
    json.add("servers", GSON.toJsonTree(proxyData.servers()));
    json.add("plugins", GSON.toJsonTree(proxyData.plugins()));
    json.add("limbos", GSON.toJsonTree(proxyData.limbos()));
    json.add("lobbies", GSON.toJsonTree(proxyData.lobbies()));
    return json;
  }
}

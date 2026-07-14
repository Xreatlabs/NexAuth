/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import com.google.gson.JsonObject;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;

public record StatusSnapshotProvider(
    String version,
    String platform,
    String failurePolicyMode,
    boolean multiProxyEnabled,
    boolean emailEnabled,
    boolean totpEnabled,
    boolean limboIntegrationAvailable,
    xyz.xreatlabs.nexauth.api.PlatformHandle.ProxyData proxyData,
    JsonObject metrics) {

  public static StatusSnapshot from(AuthenticNexAuth<?, ?> plugin) {
    return new StatusSnapshotProvider(
            plugin.getVersion(),
            plugin.getPlatformHandle().getPlatformIdentifier(),
            plugin.getFailurePolicyMode().name(),
            plugin.multiProxyEnabled(),
            plugin.getEmailHandler() != null,
            plugin.getTOTPProvider() != null,
            plugin.getLimboIntegration() != null,
            plugin.getPlatformHandle().getProxyData(),
            plugin.getAuthMetrics().toJson())
        .create();
  }

  public StatusSnapshot create() {
    return new StatusSnapshot(
        version,
        platform,
        failurePolicyMode,
        multiProxyEnabled,
        emailEnabled,
        totpEnabled,
        limboIntegrationAvailable,
        proxyData,
        metrics);
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import com.google.gson.JsonObject;
import xyz.xreatlabs.nexauth.api.PlatformHandle;

public record StatusSnapshot(
    String version,
    String platform,
    String failurePolicyMode,
    boolean multiProxyEnabled,
    boolean emailEnabled,
    boolean totpEnabled,
    boolean limboIntegrationAvailable,
    PlatformHandle.ProxyData proxyData,
    JsonObject metrics) {}

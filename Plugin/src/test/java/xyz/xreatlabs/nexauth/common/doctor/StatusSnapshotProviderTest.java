/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.PlatformHandle;

class StatusSnapshotProviderTest {

  @Test
  void buildsSnapshotFromProvidedState() {
    var metrics = new JsonObject();
    metrics.addProperty("total", 3);
    var provider =
        new StatusSnapshotProvider(
            "1.0.0",
            "test-platform",
            "DEGRADE",
            true,
            true,
            true,
            true,
            new PlatformHandle.ProxyData(
                "proxy-test",
                List.of("lobby"),
                List.of("NexAuth"),
                List.of("limbo"),
                List.of("lobby")),
            metrics);

    var snapshot = provider.create();

    assertEquals("1.0.0", snapshot.version());
    assertEquals("test-platform", snapshot.platform());
    assertEquals("DEGRADE", snapshot.failurePolicyMode());
    assertTrue(snapshot.multiProxyEnabled());
    assertTrue(snapshot.emailEnabled());
    assertTrue(snapshot.totpEnabled());
    assertTrue(snapshot.limboIntegrationAvailable());
    assertEquals("proxy-test", snapshot.proxyData().name());
    assertEquals(3, snapshot.metrics().get("total").getAsLong());
  }

  @Test
  void preservesUnavailableOptionalSystems() {
    var provider =
        new StatusSnapshotProvider(
            "1.0.0", "test-platform", "HARD_FAIL", false, false, false, false, null, null);

    var snapshot = provider.create();

    assertFalse(snapshot.multiProxyEnabled());
    assertFalse(snapshot.emailEnabled());
    assertFalse(snapshot.totpEnabled());
    assertFalse(snapshot.limboIntegrationAvailable());
  }
}

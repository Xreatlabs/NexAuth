/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.integration.nativelimbo;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.InfoForwarding.Type;

public class InfoForwardingFactory {

  public InfoForwarding none() {
    var forwarding = new InfoForwarding();
    setField(forwarding, "type", Type.NONE);
    return forwarding;
  }

  public InfoForwarding legacy() {
    var forwarding = new InfoForwarding();
    setField(forwarding, "type", Type.LEGACY);
    return forwarding;
  }

  public InfoForwarding modern(byte[] secretKey) {
    var forwarding = new InfoForwarding();
    setField(forwarding, "type", Type.MODERN);
    setField(forwarding, "secretKey", secretKey == null ? new byte[0] : secretKey);
    return forwarding;
  }

  public InfoForwarding bungeeGuard(Collection<String> tokens) {
    var forwarding = new InfoForwarding();
    setField(forwarding, "type", Type.BUNGEE_GUARD);
    setField(forwarding, "tokens", new ArrayList<>(tokens));
    return forwarding;
  }

  public InfoForwarding bungeeGuardFromSecret(byte[] secret) {
    var tokens = new ArrayList<String>();
    if (secret != null) {
      tokens.add(new String(secret, StandardCharsets.UTF_8));
    }
    return bungeeGuard(tokens);
  }

  private void setField(InfoForwarding forwarding, String fieldName, Object value) {
    try {
      Field field = InfoForwarding.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(forwarding, value);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Unable to configure info forwarding", e);
    }
  }
}

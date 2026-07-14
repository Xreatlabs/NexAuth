/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.reload;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import xyz.xreatlabs.nexauth.common.config.ConfigurateHelper;
import xyz.xreatlabs.nexauth.common.config.ConfigurationKeys;
import xyz.xreatlabs.nexauth.common.config.key.ConfigurationKey;
import xyz.xreatlabs.nexauth.common.util.GeneralUtil;

/**
 * Captures a point-in-time snapshot of all config/message values as key→string pairs. Used to
 * compute diffs before/after a reload.
 */
public record ConfigSnapshot(Map<String, String> configValues, Map<String, String> messageValues) {

  /** Capture the current state of configuration keys. */
  public static Map<String, String> captureConfig(ConfigurateHelper helper) {
    var snapshot = new LinkedHashMap<String, String>();
    for (ConfigurationKey<?> key : GeneralUtil.extractKeys(ConfigurationKeys.class)) {
      if (key.defaultValue() == null) continue; // Skip comment-only keys
      try {
        var value = key.compute(helper);
        snapshot.put(key.key(), Objects.toString(value, "<null>"));
      } catch (Exception e) {
        snapshot.put(key.key(), "<error>");
      }
    }
    return snapshot;
  }

  /** Capture the current state of message keys by walking the HOCON node tree. */
  public static Map<String, String> captureMessages(ConfigurateHelper helper) {
    var snapshot = new LinkedHashMap<String, String>();
    walkNode("", helper.configuration(), snapshot);
    return snapshot;
  }

  private static void walkNode(
      String prefix,
      org.spongepowered.configurate.CommentedConfigurationNode node,
      Map<String, String> out) {
    node.childrenMap()
        .forEach(
            (key, value) -> {
              if (!(key instanceof String str)) return;
              var fullKey = prefix.isEmpty() ? str : prefix + "." + str;
              if (value.childrenMap().isEmpty()) {
                var string = value.getString();
                out.put(fullKey, string != null ? string : "<null>");
              } else {
                walkNode(fullKey, value, out);
              }
            });
  }
}

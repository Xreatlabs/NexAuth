/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.reload;

import java.util.*;

/** Computes the diff between two config/message snapshots. */
public final class ConfigDiff {

  public enum ChangeType {
    ADDED,
    CHANGED,
    REMOVED
  }

  public record Change(String key, String oldValue, String newValue, ChangeType type) {
    public String formatInline() {
      return switch (type) {
        case ADDED -> key + ": " + newValue;
        case REMOVED -> key + ": " + oldValue + " (removed)";
        case CHANGED -> key + ": " + oldValue + " → " + newValue;
      };
    }
  }

  /** Diff two snapshots. Returns a list of changes. */
  public static List<Change> diff(Map<String, String> before, Map<String, String> after) {
    var changes = new ArrayList<Change>();

    // Check for changed or removed keys
    for (var entry : before.entrySet()) {
      var key = entry.getKey();
      var oldVal = entry.getValue();
      var newVal = after.get(key);

      if (newVal == null) {
        changes.add(new Change(key, oldVal, null, ChangeType.REMOVED));
      } else if (!Objects.equals(oldVal, newVal)) {
        changes.add(new Change(key, oldVal, newVal, ChangeType.CHANGED));
      }
    }

    // Check for added keys
    for (var entry : after.entrySet()) {
      if (!before.containsKey(entry.getKey())) {
        changes.add(new Change(entry.getKey(), null, entry.getValue(), ChangeType.ADDED));
      }
    }

    return changes;
  }
}

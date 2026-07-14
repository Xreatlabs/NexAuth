/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.reload;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigDiffTest {

  @Test
  void emptySnapshots_noDiff() {
    var changes = ConfigDiff.diff(Map.of(), Map.of());
    assertTrue(changes.isEmpty());
  }

  @Test
  void identicalSnapshots_noDiff() {
    var snapshot = Map.of("key1", "value1", "key2", "value2");
    var changes = ConfigDiff.diff(snapshot, snapshot);
    assertTrue(changes.isEmpty());
  }

  @Test
  void detectsChangedValues() {
    var before = Map.of("database.type", "nexauth-sqlite", "debug", "false");
    var after = Map.of("database.type", "nexauth-mysql", "debug", "false");

    var changes = ConfigDiff.diff(before, after);

    assertEquals(1, changes.size());
    var change = changes.get(0);
    assertEquals("database.type", change.key());
    assertEquals("nexauth-sqlite", change.oldValue());
    assertEquals("nexauth-mysql", change.newValue());
    assertEquals(ConfigDiff.ChangeType.CHANGED, change.type());
  }

  @Test
  void detectsAddedKeys() {
    var before = Map.of("key1", "val1");
    var after = Map.of("key1", "val1", "key2", "val2");

    var changes = ConfigDiff.diff(before, after);

    assertEquals(1, changes.size());
    var change = changes.get(0);
    assertEquals("key2", change.key());
    assertNull(change.oldValue());
    assertEquals("val2", change.newValue());
    assertEquals(ConfigDiff.ChangeType.ADDED, change.type());
  }

  @Test
  void detectsRemovedKeys() {
    var before = new LinkedHashMap<String, String>();
    before.put("key1", "val1");
    before.put("key2", "val2");
    var after = Map.of("key1", "val1");

    var changes = ConfigDiff.diff(before, after);

    assertEquals(1, changes.size());
    var change = changes.get(0);
    assertEquals("key2", change.key());
    assertEquals("val2", change.oldValue());
    assertNull(change.newValue());
    assertEquals(ConfigDiff.ChangeType.REMOVED, change.type());
  }

  @Test
  void detectsMultipleChangeTypes() {
    var before = new LinkedHashMap<String, String>();
    before.put("kept", "same");
    before.put("changed", "old");
    before.put("removed", "gone");

    var after = new LinkedHashMap<String, String>();
    after.put("kept", "same");
    after.put("changed", "new");
    after.put("added", "fresh");

    var changes = ConfigDiff.diff(before, after);

    assertEquals(3, changes.size());

    // Changed
    var changed = changes.stream().filter(c -> c.key().equals("changed")).findFirst().orElseThrow();
    assertEquals(ConfigDiff.ChangeType.CHANGED, changed.type());
    assertEquals("old", changed.oldValue());
    assertEquals("new", changed.newValue());

    // Removed
    var removed = changes.stream().filter(c -> c.key().equals("removed")).findFirst().orElseThrow();
    assertEquals(ConfigDiff.ChangeType.REMOVED, removed.type());

    // Added
    var added = changes.stream().filter(c -> c.key().equals("added")).findFirst().orElseThrow();
    assertEquals(ConfigDiff.ChangeType.ADDED, added.type());
  }

  @Test
  void formatInline_changed() {
    var change = new ConfigDiff.Change("db.type", "sqlite", "mysql", ConfigDiff.ChangeType.CHANGED);
    assertEquals("db.type: sqlite → mysql", change.formatInline());
  }

  @Test
  void formatInline_added() {
    var change = new ConfigDiff.Change("new.key", null, "value", ConfigDiff.ChangeType.ADDED);
    assertEquals("new.key: value", change.formatInline());
  }

  @Test
  void formatInline_removed() {
    var change = new ConfigDiff.Change("old.key", "value", null, ConfigDiff.ChangeType.REMOVED);
    assertEquals("old.key: value (removed)", change.formatInline());
  }
}

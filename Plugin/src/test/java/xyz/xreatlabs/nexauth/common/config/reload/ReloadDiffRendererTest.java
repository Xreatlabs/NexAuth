/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.reload;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

class ReloadDiffRendererTest {

  private static final PlainTextComponentSerializer PLAIN =
      PlainTextComponentSerializer.plainText();

  @Test
  void noChanges_showsNoChangesDetected() {
    var result = ReloadDiffRenderer.render(List.of(), List.of());
    var text = PLAIN.serialize(result);
    assertTrue(text.contains("No changes detected"));
  }

  @Test
  void configChangesOnly_showsConfigCount() {
    var changes =
        List.of(new ConfigDiff.Change("debug", "false", "true", ConfigDiff.ChangeType.CHANGED));
    var result = ReloadDiffRenderer.render(changes, List.of());
    var text = PLAIN.serialize(result);
    assertTrue(text.contains("1 config change"));
    assertFalse(text.contains("message change"));
  }

  @Test
  void messageChangesOnly_showsMessageCount() {
    var changes =
        List.of(new ConfigDiff.Change("kick-msg", "old", "new", ConfigDiff.ChangeType.CHANGED));
    var result = ReloadDiffRenderer.render(List.of(), changes);
    var text = PLAIN.serialize(result);
    assertTrue(text.contains("1 message change"));
    assertFalse(text.contains("config change"));
  }

  @Test
  void bothChanges_showsBothCounts() {
    var configChanges =
        List.of(
            new ConfigDiff.Change("debug", "false", "true", ConfigDiff.ChangeType.CHANGED),
            new ConfigDiff.Change("ip-limit", "-1", "5", ConfigDiff.ChangeType.CHANGED));
    var messageChanges =
        List.of(new ConfigDiff.Change("kick-msg", "old", "new", ConfigDiff.ChangeType.CHANGED));
    var result = ReloadDiffRenderer.render(configChanges, messageChanges);
    var text = PLAIN.serialize(result);
    assertTrue(text.contains("2 config changes"));
    assertTrue(text.contains("1 message change"));
  }

  @Test
  void resultHasHoverEvent() {
    var changes =
        List.of(new ConfigDiff.Change("debug", "false", "true", ConfigDiff.ChangeType.CHANGED));
    var result = ReloadDiffRenderer.render(changes, List.of());

    // Walk the component tree to find the hover event
    var hasHover = findHoverEvent(result);
    assertTrue(hasHover, "Result should contain a hover event with diff details");
  }

  @Test
  void pluralization_singular() {
    var changes = List.of(new ConfigDiff.Change("key", "a", "b", ConfigDiff.ChangeType.CHANGED));
    var text = PLAIN.serialize(ReloadDiffRenderer.render(changes, List.of()));
    assertTrue(text.contains("1 config change"));
    assertFalse(text.contains("1 config changes")); // Should NOT pluralize
  }

  @Test
  void pluralization_plural() {
    var changes =
        List.of(
            new ConfigDiff.Change("k1", "a", "b", ConfigDiff.ChangeType.CHANGED),
            new ConfigDiff.Change("k2", "c", "d", ConfigDiff.ChangeType.CHANGED));
    var text = PLAIN.serialize(ReloadDiffRenderer.render(changes, List.of()));
    assertTrue(text.contains("2 config changes")); // Should pluralize
  }

  private boolean findHoverEvent(Component component) {
    if (component.hoverEvent() != null) return true;
    for (var child : component.children()) {
      if (findHoverEvent(child)) return true;
    }
    return false;
  }
}

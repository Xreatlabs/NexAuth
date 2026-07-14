/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.reload;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Renders config diff results as Adventure components with hover text. */
public final class ReloadDiffRenderer {

  private static final int MAX_VALUE_LENGTH = 40;

  private ReloadDiffRenderer() {}

  /**
   * Build a summary component with hover details.
   *
   * @param configChanges changes from config.conf
   * @param messageChanges changes from messages.conf
   * @return a TextComponent showing the summary with hover details
   */
  public static TextComponent render(
      List<ConfigDiff.Change> configChanges, List<ConfigDiff.Change> messageChanges) {
    int configCount = configChanges.size();
    int messageCount = messageChanges.size();

    if (configCount == 0 && messageCount == 0) {
      return Component.text("✅ Reloaded! ", NamedTextColor.GREEN)
          .append(Component.text("No changes detected.", NamedTextColor.GRAY));
    }

    // Build hover text
    var hoverBuilder = Component.text();

    if (configCount > 0) {
      hoverBuilder.append(
          Component.text("Config Changes:", NamedTextColor.GOLD, TextDecoration.BOLD));
      for (var change : configChanges) {
        hoverBuilder.append(Component.newline());
        hoverBuilder.append(renderChange(change));
      }
    }

    if (messageCount > 0) {
      if (configCount > 0) {
        hoverBuilder.append(Component.newline());
        hoverBuilder.append(Component.newline());
      }
      hoverBuilder.append(
          Component.text("Message Changes:", NamedTextColor.GOLD, TextDecoration.BOLD));
      for (var change : messageChanges) {
        hoverBuilder.append(Component.newline());
        hoverBuilder.append(renderChange(change));
      }
    }

    // Build summary line
    var summary = Component.text("✅ Reloaded! ", NamedTextColor.GREEN);

    var parts = Component.text();
    if (configCount > 0) {
      parts.append(
          Component.text(
              configCount + " config change" + (configCount != 1 ? "s" : ""), NamedTextColor.AQUA));
    }
    if (messageCount > 0) {
      if (configCount > 0) {
        parts.append(Component.text(", ", NamedTextColor.GRAY));
      }
      parts.append(
          Component.text(
              messageCount + " message change" + (messageCount != 1 ? "s" : ""),
              NamedTextColor.AQUA));
    }

    parts.append(
        Component.text(" [hover for details]", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));

    return summary.append(parts.build().hoverEvent(HoverEvent.showText(hoverBuilder.build())));
  }

  private static TextComponent renderChange(ConfigDiff.Change change) {
    return switch (change.type()) {
      case ADDED ->
          Component.text("  + ", NamedTextColor.GREEN)
              .append(Component.text(change.key() + ": ", NamedTextColor.WHITE))
              .append(Component.text(truncate(change.newValue()), NamedTextColor.GREEN));
      case REMOVED ->
          Component.text("  - ", NamedTextColor.RED)
              .append(Component.text(change.key() + ": ", NamedTextColor.WHITE))
              .append(Component.text(truncate(change.oldValue()), NamedTextColor.RED));
      case CHANGED ->
          Component.text("  ~ ", NamedTextColor.YELLOW)
              .append(Component.text(change.key() + ": ", NamedTextColor.WHITE))
              .append(Component.text(truncate(change.oldValue()), NamedTextColor.RED))
              .append(Component.text(" → ", NamedTextColor.GRAY))
              .append(Component.text(truncate(change.newValue()), NamedTextColor.GREEN));
    };
  }

  private static String truncate(String value) {
    if (value == null) return "<null>";
    if (value.length() > MAX_VALUE_LENGTH) {
      return value.substring(0, MAX_VALUE_LENGTH - 3) + "...";
    }
    return value;
  }
}

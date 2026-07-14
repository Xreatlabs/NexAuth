/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.junit.jupiter.api.Test;

class PremiumAutoLoginFeedbackTest {

  @Test
  void sendsPremiumMessageImmediatelyAndDefersTitleUntilScheduledTaskRuns() throws Exception {
    var audience = new RecordingAudience();
    var message = Component.text("You have been logged in automatically!");
    var title =
        Title.title(
            Component.text("Premium"),
            Component.empty(),
            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofMillis(500)));
    var scheduled = new ArrayList<Runnable>();

    feedbackMethod()
        .invoke(null, audience, message, title, true, (Consumer<Runnable>) scheduled::add);

    assertEquals(List.of(message), audience.messages);
    assertTrue(audience.titles.isEmpty());
    assertEquals(1, scheduled.size());

    scheduled.get(0).run();

    assertEquals(List.of(title), audience.titles);
  }

  @Test
  void doesNotScheduleTitleWhenTitlesAreDisabled() throws Exception {
    var audience = new RecordingAudience();
    var message = Component.text("You have been logged in automatically!");
    var title =
        Title.title(
            Component.text("Premium"),
            Component.empty(),
            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofMillis(500)));
    var scheduled = new ArrayList<Runnable>();

    feedbackMethod()
        .invoke(null, audience, message, title, false, (Consumer<Runnable>) scheduled::add);

    assertEquals(List.of(message), audience.messages);
    assertTrue(scheduled.isEmpty());
    assertTrue(audience.titles.isEmpty());
  }

  private static Method feedbackMethod() throws Exception {
    var clazz = Class.forName("xyz.xreatlabs.nexauth.common.listener.PremiumAutoLoginFeedback");
    var method =
        clazz.getDeclaredMethod(
            "notify", Audience.class, Component.class, Title.class, boolean.class, Consumer.class);
    method.setAccessible(true);
    return method;
  }

  private static final class RecordingAudience implements Audience {
    private final List<Component> messages = new ArrayList<>();
    private final List<Title> titles = new ArrayList<>();

    @Override
    public void sendMessage(Component message) {
      messages.add(message);
    }

    @Override
    public void showTitle(Title title) {
      titles.add(title);
    }
  }
}

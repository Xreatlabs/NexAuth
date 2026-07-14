/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.listener;

import java.util.function.Consumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

final class PremiumAutoLoginFeedback {

  private PremiumAutoLoginFeedback() {}

  static void notify(
      Audience audience,
      Component message,
      Title title,
      boolean useTitles,
      Consumer<Runnable> delayedTaskScheduler) {
    audience.sendMessage(message);

    if (useTitles) {
      delayedTaskScheduler.accept(() -> audience.showTitle(title));
    }
  }
}

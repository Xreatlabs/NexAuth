/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class InvalidCommandArgumentTest {

  @Test
  void exposesUserFacingComponent() {
    var message = Component.text("Wrong password");
    var argument = new InvalidCommandArgument(message);

    assertSame(message, argument.getUserFuckUp());
    assertEquals(message, argument.getUserFuckUp());
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.event.events.AuthenticatedEvent;
import xyz.xreatlabs.nexauth.api.event.events.LimboServerChooseEvent;
import xyz.xreatlabs.nexauth.api.event.events.LobbyServerChooseEvent;
import xyz.xreatlabs.nexauth.api.event.events.PasswordChangeEvent;
import xyz.xreatlabs.nexauth.api.event.events.PremiumLoginSwitchEvent;
import xyz.xreatlabs.nexauth.api.event.events.WrongPasswordEvent;

class EventTypesTest {

  @Test
  void exposesExpectedEventClasses() {
    var types = new EventTypes<Object, Object>();

    assertEquals(AuthenticatedEvent.class, types.authenticated.getClazz());
    assertEquals(WrongPasswordEvent.class, types.wrongPassword.getClazz());
    assertEquals(LimboServerChooseEvent.class, types.limboServerChoose.getClazz());
    assertEquals(LobbyServerChooseEvent.class, types.lobbyServerChoose.getClazz());
    assertEquals(PasswordChangeEvent.class, types.passwordChange.getClazz());
    assertEquals(PremiumLoginSwitchEvent.class, types.premiumLoginSwitch.getClazz());
  }
}

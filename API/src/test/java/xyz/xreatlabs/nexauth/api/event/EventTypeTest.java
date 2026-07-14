/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class EventTypeTest {

  @Test
  void equalityAndHashCodeDependOnClass() {
    var first = new EventType<>(DummyEvent.class);
    var second = new EventType<>(DummyEvent.class);
    var third = new EventType<>(OtherEvent.class);

    assertSame(DummyEvent.class, first.getClazz());
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertNotEquals(first, third);
  }

  private interface DummyEvent extends Event<Object, Object> {}

  private interface OtherEvent extends Event<Object, Object> {}
}

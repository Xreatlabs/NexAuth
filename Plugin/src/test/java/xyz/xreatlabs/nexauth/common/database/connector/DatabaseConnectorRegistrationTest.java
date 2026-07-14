/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;

class DatabaseConnectorRegistrationTest {

  @Test
  void storesFactoryConfigClassAndId() {
    var factory =
        (xyz.xreatlabs.nexauth.api.util.ThrowableFunction<String, DummyConnector, RuntimeException>)
            DummyConnector::new;
    var registration = new DatabaseConnectorRegistration<>(factory, String.class, "sqlite");

    assertSame(factory, registration.factory());
    assertEquals(String.class, registration.configClass());
    assertEquals("sqlite", registration.id());
  }

  private static final class DummyConnector implements DatabaseConnector<RuntimeException, String> {
    private final String prefix;
    private boolean connected;

    private DummyConnector(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public void connect() {
      connected = true;
    }

    @Override
    public boolean connected() {
      return connected;
    }

    @Override
    public void disconnect() {
      connected = false;
    }

    @Override
    public String obtainInterface() {
      if (!connected) {
        throw new IllegalStateException("not connected");
      }
      return prefix;
    }

    @Override
    public <V> V runQuery(
        xyz.xreatlabs.nexauth.api.util.ThrowableFunction<String, V, RuntimeException> function) {
      return function.apply(obtainInterface());
    }
  }
}

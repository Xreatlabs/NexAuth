/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class MultipleSetterTest {

  @Test
  void appliesValuesAcrossRepeatedBlocks() throws Exception {
    var setter = new MultipleSetter();
    setter.set(1, "player");
    setter.set(2, LocalDateTime.of(2026, 3, 15, 8, 30));

    var captured = new LinkedHashMap<Integer, Object>();
    PreparedStatement statement =
        (PreparedStatement)
            Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[] {PreparedStatement.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("setObject")) {
                    captured.put((Integer) args[0], args[1]);
                    return null;
                  }
                  if (method.getName().equals("toString")) {
                    return "PreparedStatementProxy";
                  }
                  return null;
                });

    setter.apply(statement, 2);

    assertEquals(4, captured.size());
    assertEquals("player", captured.get(1));
    assertInstanceOf(java.sql.Timestamp.class, captured.get(2));
    assertEquals("player", captured.get(3));
    assertInstanceOf(java.sql.Timestamp.class, captured.get(4));
  }
}

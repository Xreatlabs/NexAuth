/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.reliability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.Logger;

class FailurePolicyHandlerTest {

  @Test
  void hardFailRunsHardFailActionAndLogsMessage() {
    var logs = new ArrayList<String>();
    var hardFail = new AtomicBoolean(false);
    var disable = new AtomicBoolean(false);
    var handler =
        new FailurePolicyHandler(logger(logs), () -> hardFail.set(true), () -> disable.set(true));

    handler.handle(FailurePolicyMode.HARD_FAIL, "database failed", null);

    assertTrue(hardFail.get());
    assertFalse(disable.get());
    assertEquals(List.of("error:database failed"), logs);
  }

  @Test
  void degradeRunsDisableAction() {
    var hardFail = new AtomicBoolean(false);
    var disable = new AtomicBoolean(false);
    var handler =
        new FailurePolicyHandler(
            logger(new ArrayList<>()), () -> hardFail.set(true), () -> disable.set(true));

    handler.handle(FailurePolicyMode.DEGRADE, "", null);

    assertFalse(hardFail.get());
    assertTrue(disable.get());
  }

  private static Logger logger(List<String> logs) {
    return new Logger() {
      @Override
      public void info(String message) {
        logs.add("info:" + message);
      }

      @Override
      public void info(String message, Throwable throwable) {
        logs.add("info:" + message);
      }

      @Override
      public void warn(String message) {
        logs.add("warn:" + message);
      }

      @Override
      public void warn(String message, Throwable throwable) {
        logs.add("warn:" + message);
      }

      @Override
      public void error(String message) {
        logs.add("error:" + message);
      }

      @Override
      public void error(String message, Throwable throwable) {
        logs.add("error:" + message);
      }

      @Override
      public void debug(String message) {
        logs.add("debug:" + message);
      }

      @Override
      public void debug(String message, Throwable throwable) {
        logs.add("debug:" + message);
      }
    };
  }
}

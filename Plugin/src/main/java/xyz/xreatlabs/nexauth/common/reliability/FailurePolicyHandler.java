/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.reliability;

import xyz.xreatlabs.nexauth.api.Logger;

public class FailurePolicyHandler {

  private final Logger logger;
  private final Runnable hardFailAction;
  private final Runnable disableAction;

  public FailurePolicyHandler(Logger logger, Runnable hardFailAction, Runnable disableAction) {
    this.logger = logger;
    this.hardFailAction = hardFailAction;
    this.disableAction = disableAction;
  }

  public void handle(FailurePolicyMode mode, String message, Throwable throwable) {
    if (message != null && !message.isBlank()) {
      logger.error(message);
    }

    if (throwable != null) {
      throwable.printStackTrace();
    }

    switch (mode) {
      case HARD_FAIL -> hardFailAction.run();
      case RETRY_THEN_DISABLE, DEGRADE -> disableAction.run();
    }
  }
}

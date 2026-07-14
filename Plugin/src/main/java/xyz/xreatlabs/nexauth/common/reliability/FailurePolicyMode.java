/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.reliability;

public enum FailurePolicyMode {
  HARD_FAIL,
  RETRY_THEN_DISABLE,
  DEGRADE;

  public static FailurePolicyMode parse(String value) {
    if (value == null) {
      return HARD_FAIL;
    }

    try {
      return FailurePolicyMode.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ignored) {
      return HARD_FAIL;
    }
  }
}

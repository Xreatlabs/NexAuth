/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

public enum DoctorSeverity {
  OK,
  WARN,
  FAIL;

  public static DoctorSeverity max(DoctorSeverity left, DoctorSeverity right) {
    return left.ordinal() >= right.ordinal() ? left : right;
  }
}

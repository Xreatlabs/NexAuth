/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.util;

public final class Colors {

  private static final char CHAR_FROM = '&';
  private static final char CHAR_TO = '\u00a7';

  private Colors() {}

  public static String of(String text) {
    if (text == null) return null;
    return text.replace(CHAR_FROM, CHAR_TO);
  }
}

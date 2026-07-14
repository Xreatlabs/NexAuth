/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Log {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss");
  private static int debugLevel = Level.INFO.getIndex();

  private Log() {}

  public static int getLevel() {
    return debugLevel;
  }

  public static void info(Object msg, Object... args) {
    print(Level.INFO, msg, null, args);
  }

  public static void debug(Object msg, Object... args) {
    print(Level.DEBUG, msg, null, args);
  }

  public static void warning(Object msg, Object... args) {
    print(Level.WARNING, msg, null, args);
  }

  public static void warning(Object msg, Throwable t, Object... args) {
    print(Level.WARNING, msg, t, args);
  }

  public static void error(Object msg, Object... args) {
    print(Level.ERROR, msg, null, args);
  }

  public static void error(Object msg, Throwable t, Object... args) {
    print(Level.ERROR, msg, t, args);
  }

  public static void print(Level level, Object msg, Throwable t, Object... args) {
    if (debugLevel >= level.getIndex()) {
      String output =
          String.format("%s: %s", getPrefix(level), String.format(msg.toString(), args));
      System.out.print(output);
      if (t != null) t.printStackTrace();
    }
  }

  public static boolean isDebug() {
    return debugLevel >= Level.DEBUG.getIndex();
  }

  private static String getPrefix(Level level) {
    return String.format("[%s] [%s]", getTime(), level.getDisplay());
  }

  private static String getTime() {
    return LocalTime.now().format(FORMATTER);
  }

  static void setLevel(int level) {
    debugLevel = level;
  }

  public enum Level {
    ERROR("ERROR", 0),
    WARNING("WARNING", 1),
    INFO("INFO", 2),
    DEBUG("DEBUG", 3);

    private final String display;
    private final int index;

    Level(String display, int index) {
      this.display = display;
      this.index = index;
    }

    public String getDisplay() {
      return display;
    }

    public int getIndex() {
      return index;
    }
  }
}

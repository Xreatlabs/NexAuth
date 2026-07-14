/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo;

import java.nio.file.Paths;
import ua.nanit.limbo.configuration.YamlLimboConfig;
import ua.nanit.limbo.server.ConsoleCommandHandler;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NexLimbo {

  public static void main(String[] args) {
    try {
      ConsoleCommandHandler consoleCommandHandler = new ConsoleCommandHandler();
      ClassLoader classLoader = LimboServer.class.getClassLoader();
      LimboServer server =
          new LimboServer(
              new YamlLimboConfig(Paths.get("./"), classLoader).load(),
              consoleCommandHandler,
              classLoader);
      consoleCommandHandler.registerAll(server).start();
      server.start();
      Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "NexLimbo shutdown thread"));
    } catch (Exception e) {
      Log.error("Cannot start server: ", e);
    }
  }
}

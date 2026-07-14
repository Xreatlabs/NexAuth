/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.commands;

import java.util.Collection;
import ua.nanit.limbo.server.Command;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public class CmdHelp implements Command {

  private final LimboServer server;

  public CmdHelp(LimboServer server) {
    this.server = server;
  }

  @Override
  public void execute() {
    Collection<Command> commands = server.getCommandManager().getCommands();

    Log.info("Available commands:");

    for (Command command : commands) {
      Log.info("%s - %s", command.name(), command.description());
    }
  }

  @Override
  public String name() {
    return "help";
  }

  @Override
  public String description() {
    return "Show this message";
  }
}

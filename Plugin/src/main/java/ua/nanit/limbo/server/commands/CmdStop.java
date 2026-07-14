/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.commands;

import ua.nanit.limbo.server.Command;
import ua.nanit.limbo.server.LimboServer;

public class CmdStop implements Command {

  private final LimboServer server;

  public CmdStop(LimboServer server) {
    this.server = server;
  }

  @Override
  public void execute() {
    server.stop();
  }

  @Override
  public String name() {
    return "stop";
  }

  @Override
  public String description() {
    return "Stop the server";
  }
}

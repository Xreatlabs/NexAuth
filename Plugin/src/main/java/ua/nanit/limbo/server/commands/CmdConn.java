/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.commands;

import ua.nanit.limbo.server.Command;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public class CmdConn implements Command {

  private final LimboServer server;

  public CmdConn(LimboServer server) {
    this.server = server;
  }

  @Override
  public void execute() {
    Log.info("Connections: %d", server.getConnections().getCount());
  }

  @Override
  public String name() {
    return "conn";
  }

  @Override
  public String description() {
    return "Display connections count";
  }
}

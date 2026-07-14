/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.play;

import java.util.List;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

/** Packet for 1.13+ */
public class PacketDeclareCommands implements PacketOut {

  private List<String> commands;

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeVarInt(commands.size() * 2 + 1); // +1 because declaring root node

    // Declare root node

    msg.writeByte(0);
    msg.writeVarInt(commands.size());

    for (int i = 1; i <= commands.size() * 2; i++) {
      msg.writeVarInt(i++);
    }

    // Declare other commands

    int i = 1;
    for (String cmd : commands) {
      msg.writeByte(1 | 0x04);
      msg.writeVarInt(1);
      msg.writeVarInt(i + 1);
      msg.writeString(cmd);
      i++;

      msg.writeByte(2 | 0x04 | 0x10);
      msg.writeVarInt(1);
      msg.writeVarInt(i);
      msg.writeString("arg");
      msg.writeString("brigadier:string");
      msg.writeVarInt(0);
      msg.writeString("minecraft:ask_server");
      i++;
    }

    msg.writeVarInt(0);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

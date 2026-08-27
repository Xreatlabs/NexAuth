/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Empty command tree (1.13+): one root node, no children, no arguments. */
public final class PacketDeclareCommands {

  public void write(ByteBuf buf, int protocol) {
    Buf.writeVarInt(buf, 1); // node count
    buf.writeByte(0); // root: literal-less, executable-less
    Buf.writeVarInt(buf, 0); // child count
    Buf.writeVarInt(buf, 0); // root node index
  }
}

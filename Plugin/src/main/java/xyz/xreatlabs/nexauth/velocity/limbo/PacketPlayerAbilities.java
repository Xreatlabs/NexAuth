/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Abilities: flags 0x02 (flying allowed), speeds 0.0/0.1. */
public final class PacketPlayerAbilities {

  public void write(ByteBuf buf, int protocol) {
    buf.writeByte(0x02);
    buf.writeFloat(0.0F);
    buf.writeFloat(0.1F);
  }
}

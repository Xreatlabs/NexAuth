/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Game event 13 (start waiting for chunks), value 0. Sent 1.20.3+. */
public final class PacketStartWaitingChunks {

  public void write(ByteBuf buf, int protocol) {
    buf.writeByte(13);
    buf.writeFloat(0);
  }
}

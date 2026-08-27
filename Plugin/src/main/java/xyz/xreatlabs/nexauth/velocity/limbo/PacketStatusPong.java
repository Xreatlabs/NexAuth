/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Status ping echo: the 8-byte long verbatim. */
public final class PacketStatusPong {

  private final long payload;

  public PacketStatusPong(long payload) {
    this.payload = payload;
  }

  public void write(ByteBuf buf, int protocol) {
    buf.writeLong(payload);
  }
}

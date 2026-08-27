/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;

/** Legacy-JSON disconnect used from the login and configuration states. */
public final class PacketDisconnect {

  private final String reason;

  public PacketDisconnect(String reason) {
    this.reason = reason;
  }

  public void write(ByteBuf buf, int protocol) {
    Buf.writeString(buf, "{\"text\": \"" + reason + "\"}");
  }
}

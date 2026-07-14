/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.login;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.registry.Version;

class PacketLoginStartTest {

  @Test
  void decodesUuidForCurrent26_1Protocol() {
    UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000775");
    ByteMessage message = new ByteMessage(Unpooled.buffer());
    message.writeString("LatestClient");
    message.writeUuid(uuid);

    PacketLoginStart packet = new PacketLoginStart();
    packet.decode(message, Version.of(775));

    assertEquals("LatestClient", packet.getUsername());
    assertEquals(uuid, packet.getUuid());
    assertEquals(0, message.readableBytes());
  }
}

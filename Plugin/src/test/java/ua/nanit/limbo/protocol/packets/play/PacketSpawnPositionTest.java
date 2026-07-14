package ua.nanit.limbo.protocol.packets.play;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.registry.Version;

class PacketSpawnPositionTest {

  @Test
  void encodesRespawnDataForProtocol773AndNewer() {
    ByteMessage message = new ByteMessage(Unpooled.buffer());

    new PacketSpawnPosition("minecraft:the_nether", 1, 64, 2).encode(message, Version.V1_21_9);

    assertEquals("minecraft:the_nether", message.readString());
    assertEquals(encodePosition(1, 64, 2), message.readLong());
    assertEquals(0.0F, message.readFloat());
    assertEquals(0.0F, message.readFloat());
    assertEquals(0, message.readableBytes());
  }

  @Test
  void keepsLegacySpawnPositionShapeBeforeProtocol773() {
    ByteMessage message = new ByteMessage(Unpooled.buffer());

    new PacketSpawnPosition("minecraft:the_nether", 1, 64, 2).encode(message, Version.V1_21_7);

    assertEquals(encodePosition(1, 64, 2), message.readLong());
    assertEquals(0.0F, message.readFloat());
    assertEquals(0, message.readableBytes());
  }

  private static long encodePosition(long x, long y, long z) {
    return ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class ForwardingTest {

  private static final byte[] SECRET = "s3cret-velocity-key".getBytes(StandardCharsets.UTF_8);

  private static final String NUL = String.valueOf((char) 0);

  private static ByteBuf modernPayload(int version, String address, UUID uuid, String username)
      throws Exception {
    ByteBuf signed = Unpooled.buffer();
    Buf.writeVarInt(signed, version);
    Buf.writeString(signed, address);
    Buf.writeUuid(signed, uuid);
    Buf.writeString(signed, username);

    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
    byte[] signature = mac.doFinal(io.netty.buffer.ByteBufUtil.getBytes(signed));

    ByteBuf payload = Unpooled.buffer();
    payload.writeBytes(signature);
    payload.writeBytes(io.netty.buffer.ByteBufUtil.getBytes(signed));
    signed.release();
    return payload;
  }

  @Test
  void modernPayloadVerifiesWithCorrectSignature() throws Exception {
    UUID uuid = UUID.randomUUID();
    ByteBuf payload = modernPayload(1, "127.0.0.1", uuid, "Kraters");

    Forwarding.Profile profile = Forwarding.verifyModern(payload, SECRET);
    assertNotNull(profile, "correctly signed payload must verify");
    assertEquals("127.0.0.1", profile.address());
    assertEquals(uuid, profile.uuid());
    assertEquals("Kraters", profile.username());
    payload.release();
  }

  @Test
  void modernPayloadVerifiesFromMidPacketReaderIndex() throws Exception {
    // Production framing: verifyModern receives a frame slice whose packet id, message id and
    // success boolean were already consumed, so the signature starts at the current reader index.
    UUID uuid = UUID.randomUUID();
    ByteBuf payload = modernPayload(1, "10.0.0.7", uuid, "Kraters");
    ByteBuf framed = Unpooled.buffer();
    framed.writeByte(0x04); // packet id
    Buf.writeVarInt(framed, 42); // message id
    framed.writeBoolean(true); // successful
    framed.writeBytes(payload);
    payload.release();

    Buf.readVarInt(framed); // packet id, as channelRead0 does
    Buf.readVarInt(framed); // message id, as handleLoginPluginResponse does
    framed.readBoolean(); // successful

    Forwarding.Profile profile = Forwarding.verifyModern(framed, SECRET);
    assertNotNull(profile, "payload framed like a real login plugin response must verify");
    assertEquals("10.0.0.7", profile.address());
    assertEquals(uuid, profile.uuid());
    assertEquals("Kraters", profile.username());
    framed.release();
  }

  @Test
  void modernPayloadRejectsTamperedSignature() throws Exception {
    ByteBuf payload = modernPayload(1, "127.0.0.1", UUID.randomUUID(), "Kraters");
    payload.setByte(0, payload.getByte(0) ^ 0xFF); // flip first signature byte

    assertNull(Forwarding.verifyModern(payload, SECRET), "tampered signature must fail");
    payload.release();
  }

  @Test
  void modernPayloadRejectsWrongVersionInt() throws Exception {
    ByteBuf payload = modernPayload(2, "127.0.0.1", UUID.randomUUID(), "Kraters");

    assertNull(Forwarding.verifyModern(payload, SECRET), "version != 1 must fail");
    payload.release();
  }

  @Test
  void modernPayloadRejectsWrongSecret() throws Exception {
    ByteBuf payload = modernPayload(1, "127.0.0.1", UUID.randomUUID(), "Kraters");

    assertNull(
        Forwarding.verifyModern(payload, "other-key".getBytes(StandardCharsets.UTF_8)),
        "signature computed with a different secret must fail");
    payload.release();
  }

  @Test
  void legacyHostSplitsIntoAddressAndUuid() {
    UUID uuid = UUID.randomUUID();
    Forwarding.LegacyHost split =
        Forwarding.splitLegacy("proxyhost" + NUL + "10.0.0.5" + NUL + uuid);

    assertNotNull(split);
    assertEquals("10.0.0.5", split.host());
    assertEquals(uuid, split.uuid());
  }

  @Test
  void legacyHostRejectsMissingParts() {
    assertNull(Forwarding.splitLegacy("no-separators-at-all"));
    assertNull(Forwarding.splitLegacy("a" + NUL + "b"));
  }

  @Test
  void bungeeGuardAcceptsValidToken() {
    UUID uuid = UUID.randomUUID();
    String host =
        "proxy"
            + NUL
            + "10.0.0.9"
            + NUL
            + uuid
            + NUL
            + "[{\"name\":\"bungeeguard-token\",\"value\":\"tok123\"}]";

    Forwarding.Profile profile = Forwarding.verifyBungeeGuard(host, List.of("tok123"));
    assertNotNull(profile);
    assertEquals("10.0.0.9", profile.address());
    assertEquals(uuid, profile.uuid());
  }

  @Test
  void bungeeGuardRejectsBadToken() {
    String host =
        "proxy"
            + NUL
            + "10.0.0.9"
            + NUL
            + UUID.randomUUID()
            + NUL
            + "[{\"name\":\"bungeeguard-token\",\"value\":\"wrong\"}]";

    assertNull(Forwarding.verifyBungeeGuard(host, List.of("tok123")));
  }

  @Test
  void bungeeGuardRejectsMalformedProperties() {
    String host = "proxy" + NUL + "10.0.0.9" + NUL + UUID.randomUUID() + NUL + "not-json";

    assertNull(Forwarding.verifyBungeeGuard(host, List.of("tok123")));
  }

  @Test
  void offlineUuidMatchesNameUUIDFromBytes() {
    UUID expected =
        UUID.nameUUIDFromBytes("OfflinePlayer:Kraters".getBytes(StandardCharsets.UTF_8));

    assertEquals(expected, Forwarding.offlineUuid("Kraters"));
  }

  @Test
  void uuidFromStringParsesUndashedHex() {
    UUID dashed = UUID.randomUUID();
    String undashed = dashed.toString().replace("-", "");

    assertEquals(dashed, Forwarding.uuidFromString(undashed));
    assertEquals(dashed, Forwarding.uuidFromString(dashed.toString()));
  }
}

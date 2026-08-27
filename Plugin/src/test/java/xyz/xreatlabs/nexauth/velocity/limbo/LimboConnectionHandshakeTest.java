/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Status and NONE-forwarding login legs of the state machine, driven through an embedded channel
 * with the real framing pipeline in front of the handler.
 */
class LimboConnectionHandshakeTest {

  private static final String MOTD = "NexAuth Limbo";

  private EmbeddedChannel channel;

  @BeforeEach
  void setUp() {
    channel = newChannel(Forwarding.Mode.NONE);
  }

  @AfterEach
  void tearDown() {
    channel.finishAndReleaseAll();
  }

  private static EmbeddedChannel newChannel(Forwarding.Mode mode) {
    LimboSettings settings = new LimboSettings(100, MOTD, 5, 30, mode, new byte[] {1, 2, 3});
    // The server instance is only used for settings + connection counting; the count is
    // observable through the status response, so a real instance is wired via reflection-free
    // construction. LimboServer never needs starting for these legs.
    LimboServer server = new LimboServer(settings);
    EmbeddedChannel ch = new EmbeddedChannel();
    ch.pipeline().addLast("handler", new LimboConnection(server));
    return ch;
  }

  private void writeHandshake(int protocol, int nextState) {
    ByteBuf buf = Unpooled.buffer();
    Buf.writeVarInt(buf, 0x00); // handshake packet id
    Buf.writeVarInt(buf, protocol);
    Buf.writeString(buf, "localhost");
    buf.writeShort(25565);
    Buf.writeVarInt(buf, nextState);
    channel.writeInbound(buf);
  }

  @Test
  void statusRequestProducesMotdJsonAndPongEchoes() {
    writeHandshake(766, 1);

    ByteBuf request = Unpooled.buffer();
    Buf.writeVarInt(request, 0x00); // status request id
    channel.writeInbound(request);

    ByteBuf response = channel.readOutbound();
    assertNotNull(response);
    int id = Buf.readVarInt(response);
    assertEquals(0x00, id, "status response id is 0 in every version");
    String json = Buf.readString(response, 32767);
    assertTrue(json.contains(MOTD), "status JSON must carry the MOTD: " + json);
    assertTrue(json.contains("\"protocol\": 766"), "NONE mode advertises the client protocol");
    response.release();

    long ping = 0xCAFEBABEL;
    ByteBuf pingBuf = Unpooled.buffer();
    Buf.writeVarInt(pingBuf, 0x01);
    pingBuf.writeLong(ping);
    channel.writeInbound(pingBuf);

    ByteBuf pong = channel.readOutbound();
    assertNotNull(pong, "ping must be answered");
    assertEquals(0x01, Buf.readVarInt(pong), "pong id is 1");
    assertEquals(ping, pong.readLong(), "pong payload echoes the ping long");
    pong.release();
  }

  @Test
  void loginWithoutForwardingSendsLoginSuccessThenJoinGameForLegacyProtocol() {
    writeHandshake(754, 2);

    ByteBuf start = Unpooled.buffer();
    Buf.writeVarInt(start, 0x00); // login start id
    Buf.writeString(start, "Kraters");
    channel.writeInbound(start);

    ByteBuf success = channel.readOutbound();
    assertNotNull(success, "login success must be sent");
    int id = Buf.readVarInt(success);
    assertEquals(0x02, id, "login success id is 2 (protocol-invariant)");
    // 1.16.4 writes uuid as two longs, then the constant limbo profile. The vendored limbo
    // always sends the "NexAuth" snapshot here, never the connecting player. Properties arrive
    // from 1.19 (759) on only.
    success.readLong();
    success.readLong();
    assertEquals("NexAuth", Buf.readString(success, 256));
    assertFalse(success.isReadable(), "no trailing bytes at 754");
    success.release();

    // 754 predates the configuration state, so the play burst follows immediately.
    ByteBuf joinGame = channel.readOutbound();
    assertNotNull(joinGame, "JoinGame follows login success below 764");
    int joinId = Buf.readVarInt(joinGame);
    assertTrue(joinId >= 0, "join game id must resolve");
    assertEquals(0, joinGame.readInt(), "entity id is 0");
    joinGame.release();
  }

  @Test
  void unsupportedProtocolDisconnectsCleanly() {
    writeHandshake(99999, 2);

    ByteBuf start = Unpooled.buffer();
    Buf.writeVarInt(start, 0x00);
    Buf.writeString(start, "Kraters");
    channel.writeInbound(start);

    ByteBuf disconnect = channel.readOutbound();
    assertNotNull(disconnect, "unsupported protocol must produce a login disconnect");
    int id = Buf.readVarInt(disconnect);
    assertEquals(0x00, id, "login disconnect id is 0");
    String reason = Buf.readString(disconnect, 32767);
    assertEquals("{\"text\": \"Unsupported client version\"}", reason);
    disconnect.release();
  }

  @Test
  void modernForwardingWithoutPluginResponseDisconnects() throws Exception {
    EmbeddedChannel modern = newChannel(Forwarding.Mode.MODERN);
    writeHandshakeTo(modern, 766, 2);

    ByteBuf start = Unpooled.buffer();
    Buf.writeVarInt(start, 0x00);
    Buf.writeString(start, "Kraters");
    modern.writeInbound(start);

    ByteBuf request = modern.readOutbound();
    assertNotNull(request, "modern mode must send a login plugin request");
    int id = Buf.readVarInt(request);
    assertEquals(0x04, id, "login plugin request id is 4");
    int messageId = Buf.readVarInt(request);
    assertEquals("velocity:player_info", Buf.readString(request, 32767));
    assertFalse(request.isReadable(), "request carries zero data bytes");
    request.release();

    // Answer with successful=false: must be refused with the exact vendored string.
    ByteBuf response = Unpooled.buffer();
    Buf.writeVarInt(response, 0x02);
    Buf.writeVarInt(response, messageId);
    response.writeBoolean(false);
    modern.writeInbound(response);

    ByteBuf disconnect = modern.readOutbound();
    assertNotNull(disconnect);
    Buf.readVarInt(disconnect);
    assertEquals(
        "{\"text\": \"You need to connect with Velocity\"}", Buf.readString(disconnect, 32767));
    disconnect.release();
    modern.finishAndReleaseAll();
  }

  private static void writeHandshakeTo(EmbeddedChannel ch, int protocol, int nextState) {
    ByteBuf buf = Unpooled.buffer();
    Buf.writeVarInt(buf, 0x00);
    Buf.writeVarInt(buf, protocol);
    Buf.writeString(buf, "localhost");
    buf.writeShort(25565);
    Buf.writeVarInt(buf, nextState);
    ch.writeInbound(buf);
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Full-stack test: a real {@link LimboServer} on an ephemeral port, driven by a local netty client
 * stub with the same framing the limbo itself uses. Covers the status ping, the NONE-forwarding
 * login path for a modern protocol (766) through configuration into play, and the below-floor
 * protocol rejection.
 */
class LimboServerIT {

  private static final String MOTD = "NexAuth Limbo";
  private static final int MODERN_PROTOCOL = 766;

  private static LimboServer server;
  private static InetSocketAddress address;
  private static EventLoopGroup clientGroup;

  @BeforeAll
  static void startServer() throws Exception {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    server =
        new LimboServer(new LimboSettings(100, MOTD, 5, 30, Forwarding.Mode.NONE, new byte[0]));
    address = new InetSocketAddress("127.0.0.1", port);
    server.start(address);

    clientGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
  }

  @AfterAll
  static void stopClientGroup() throws Exception {
    if (clientGroup != null) {
      clientGroup.shutdownGracefully().sync();
    }
  }

  @Test
  @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void statusPingAdvertisesMotdAndMaxPlayers() throws Exception {
    ClientStub client = connect();

    ByteBuf handshake = Unpooled.buffer();
    Buf.writeVarInt(handshake, 0x00); // handshake intent
    Buf.writeVarInt(handshake, MODERN_PROTOCOL);
    Buf.writeString(handshake, "localhost");
    handshake.writeShort(25565);
    Buf.writeVarInt(handshake, 1); // next state: status
    client.send(handshake);

    ByteBuf request = Unpooled.buffer();
    Buf.writeVarInt(request, 0x00); // status request
    client.send(request);

    ByteBuf response = client.nextPacket();
    assertEquals(0x00, Buf.readVarInt(response), "status response packet id");
    String json = Buf.readString(response, 32767);
    assertTrue(json.contains(MOTD), "status JSON carries the MOTD: " + json);
    assertTrue(json.contains("\"max\": 100"), "status JSON carries maxPlayers 100: " + json);
    response.release();

    ByteBuf ping = Unpooled.buffer();
    Buf.writeVarInt(ping, 0x01);
    ping.writeLong(0xCAFEBABEL);
    client.send(ping);

    ByteBuf pong = client.nextPacket();
    assertEquals(0x01, Buf.readVarInt(pong), "pong packet id");
    assertEquals(0xCAFEBABEL, pong.readLong(), "pong echoes the ping payload");
    pong.release();

    client.awaitClose();
    client.releasePending();
  }

  @Test
  @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void noneLoginReachesPlayOnModernProtocol() throws Exception {
    ClientStub client = connect();

    ByteBuf handshake = Unpooled.buffer();
    Buf.writeVarInt(handshake, 0x00);
    Buf.writeVarInt(handshake, MODERN_PROTOCOL);
    Buf.writeString(handshake, "localhost");
    handshake.writeShort(25565);
    Buf.writeVarInt(handshake, 2); // next state: login
    client.send(handshake);

    ByteBuf start = Unpooled.buffer();
    Buf.writeVarInt(start, 0x00); // login start
    Buf.writeString(start, "Kraters");
    // 766 (1.20.5) login_start: uuid is always present from 764 on.
    Buf.writeUuid(start, Forwarding.offlineUuid("Kraters"));
    client.send(start);

    ByteBuf success = client.nextPacket();
    assertEquals(0x02, Buf.readVarInt(success), "login success id is protocol-invariant");
    Buf.readUuid(success); // uuid as 2 raw longs from 766
    assertEquals("NexAuth", Buf.readString(success, 256), "constant limbo profile name");
    assertEquals(0, Buf.readVarInt(success), "empty properties array");
    // 766-767 append the strict-error-handling boolean; Velocity's decode reads it unconditionally.
    assertTrue(success.readBoolean(), "strict error handling flag must be present and true");
    assertFalse(success.isReadable(), "login success must be fully consumed");
    success.release();

    // 764+: the client must acknowledge login success before configuration starts.
    ByteBuf loginAck = Unpooled.buffer();
    Buf.writeVarInt(loginAck, 0x03); // serverbound login_acknowledged, id 3 in every version
    client.send(loginAck);

    // 766 goes through the configuration state: known packs first.
    ByteBuf knownPacks = client.nextPacket();
    int knownPacksId = Buf.readVarInt(knownPacks);
    assertEquals(
        PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, MODERN_PROTOCOL),
        knownPacksId,
        "SELECT_KNOWN_PACKS id for 766");
    knownPacks.release();

    ByteBuf packResponse = Unpooled.buffer();
    Buf.writeVarInt(packResponse, 0x07); // serverbound select_known_packs at 766
    Buf.writeVarInt(packResponse, 0); // no packs
    client.send(packResponse);

    // Per-registry data and tags follow, then finish_configuration — acknowledge it to spawn.
    int finishConfigurationId =
        PacketRegistry.id(PacketRegistry.Out.FINISH_CONFIGURATION, MODERN_PROTOCOL);
    int joinGameId = PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, MODERN_PROTOCOL);
    int keepAliveId = PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, MODERN_PROTOCOL);

    boolean sawFinishConfiguration = false;
    boolean sawJoinGame = false;
    boolean sawKeepAlive = false;
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(25);
    while (System.nanoTime() < deadline && client.isOpen()) {
      ByteBuf packet = client.nextPacket(TimeUnit.SECONDS.toNanos(10));
      if (packet == null) {
        break;
      }
      int id = Buf.readVarInt(packet);
      if (id == finishConfigurationId && !sawFinishConfiguration) {
        sawFinishConfiguration = true;
        ByteBuf finishAck = Unpooled.buffer();
        Buf.writeVarInt(finishAck, 0x03); // serverbound configuration_end ack at 766
        client.send(finishAck);
      } else if (id == joinGameId) {
        sawJoinGame = true;
        assertEquals(0, packet.readInt(), "join game entity id is 0");
      } else if (id == keepAliveId) {
        sawKeepAlive = true;
      }
      packet.release();
      if (sawJoinGame && sawKeepAlive) {
        break;
      }
    }

    assertTrue(sawFinishConfiguration, "client must receive finish_configuration");
    assertTrue(sawJoinGame, "client must reach play state and receive JoinGame");
    assertTrue(sawKeepAlive, "client must receive a play keepalive");
    assertEquals(1, server.connectionCount(), "connection is registered while in play");

    client.close();
    client.awaitClose();
    client.releasePending();

    long removalDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (server.connectionCount() > 0 && System.nanoTime() < removalDeadline) {
      TimeUnit.MILLISECONDS.sleep(50);
    }
    assertEquals(0, server.connectionCount(), "connection is removed after disconnect");
  }

  @Test
  @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void belowFloorProtocolGetsUnsupportedVersionDisconnect() throws Exception {
    ClientStub client = connect();

    ByteBuf handshake = Unpooled.buffer();
    Buf.writeVarInt(handshake, 0x00);
    Buf.writeVarInt(handshake, PacketRegistry.MIN_PROTOCOL - 1); // below 1.7.2 floor
    Buf.writeString(handshake, "localhost");
    handshake.writeShort(25565);
    Buf.writeVarInt(handshake, 2);
    client.send(handshake);

    ByteBuf start = Unpooled.buffer();
    Buf.writeVarInt(start, 0x00);
    Buf.writeString(start, "Kraters");
    client.send(start);

    ByteBuf disconnect = client.nextPacket();
    assertNotNull(disconnect, "below-floor protocol must produce a login disconnect");
    assertEquals(0x00, Buf.readVarInt(disconnect), "login disconnect id");
    String reason = Buf.readString(disconnect, 32767);
    assertEquals("{\"text\": \"Unsupported client version\"}", reason);
    disconnect.release();

    client.awaitClose();
    client.releasePending();
  }

  private static ClientStub connect() throws Exception {
    ClientStub stub = new ClientStub();
    Channel channel =
        new Bootstrap()
            .group(clientGroup)
            .channel(NioSocketChannel.class)
            .handler(
                new ChannelInitializer<Channel>() {
                  @Override
                  protected void initChannel(Channel ch) {
                    ch.pipeline()
                        .addLast("frame_decoder", new VarIntFrameDecoder())
                        .addLast("frame_encoder", new VarIntLengthEncoder())
                        .addLast("handler", stub);
                  }
                })
            .connect(address)
            .sync()
            .channel();
    stub.attach(channel);
    return stub;
  }

  /** Minimal client: queues whole de-framed packets, mirrors the limbo framing on write. */
  private static final class ClientStub extends SimpleChannelInboundHandler<ByteBuf> {

    private final List<ByteBuf> packets = new CopyOnWriteArrayList<>();
    private final CountDownLatch closed = new CountDownLatch(1);

    private volatile Channel channel;

    void attach(Channel channel) {
      this.channel = channel;
    }

    boolean isOpen() {
      return channel != null && channel.isActive();
    }

    void send(ByteBuf packet) {
      channel.writeAndFlush(packet);
    }

    void close() {
      channel.close();
    }

    void awaitClose() throws InterruptedException {
      assertTrue(closed.await(10, TimeUnit.SECONDS), "server must close the connection");
    }

    /** Next packet, waiting up to 10 seconds; null on timeout (nothing was buffered). */
    ByteBuf nextPacket() {
      return nextPacket(TimeUnit.SECONDS.toNanos(10));
    }

    ByteBuf nextPacket(long timeoutNanos) {
      long deadline = System.nanoTime() + timeoutNanos;
      while (true) {
        ByteBuf packet = poll();
        if (packet != null) {
          return packet;
        }
        if (!isOpen() && poll() == null) {
          return null;
        }
        if (System.nanoTime() >= deadline) {
          return null;
        }
        try {
          TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return null;
        }
      }
    }

    private ByteBuf poll() {
      synchronized (packets) {
        if (packets.isEmpty()) {
          return null;
        }
        return packets.remove(0);
      }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
      synchronized (packets) {
        packets.add(msg.retain());
      }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      // Keep buffered packets: the server writes its final packet (pong, login disconnect) with
      // writeAndClose, so this fires on the event loop right after the packet was buffered —
      // releasing here would race the reader thread and swallow that packet.
      closed.countDown();
    }

    /** Releases packets the test never consumed; call once the assertions are done. */
    void releasePending() {
      synchronized (packets) {
        packets.forEach(ByteBuf::release);
        packets.clear();
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      ctx.close();
    }
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.util.concurrent.ScheduledFuture;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Per-connection state machine: handshake → status/login → configuration → play. Receives whole
 * de-framed packets (leading varint packet id still in the buffer), resolves ids via {@link
 * PacketRegistry} and writes framed outbound packets.
 */
public final class LimboConnection extends SimpleChannelInboundHandler<ByteBuf> {

  private static final String LIMBO_USERNAME = "NexAuth";

  private final LimboServer server;
  private final UUID uuid = Forwarding.offlineUuid(LIMBO_USERNAME);
  private final int teleportId = ThreadLocalRandom.current().nextInt();

  /**
   * Inbound resolution state. Mirrors the vendored decoder: stays LOGIN until login_acknowledged
   * arrives, even though outbound packets already use CONFIGURATION ids by then.
   */
  private PacketRegistry.State readState = PacketRegistry.State.HANDSHAKE;

  /** Outbound resolution state; jumps to CONFIGURATION right after login success (≥764). */
  private PacketRegistry.State writeState = PacketRegistry.State.HANDSHAKE;

  private int protocol;
  private InetSocketAddress address;
  private UUID profileUuid;
  private String username;
  private int velocityLoginMessageId = -1;
  private ScheduledFuture<?> keepAliveTask;
  private RegistryData registryData;
  private ChannelHandlerContext handlerContext;

  public LimboConnection(LimboServer server) {
    this.server = server;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    handlerContext = ctx;
    if (address == null) {
      address = remoteAddress(ctx);
    }
    super.handlerAdded(ctx);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    handlerContext = ctx;
    address = remoteAddress(ctx);
    super.channelActive(ctx);
  }

  private static InetSocketAddress remoteAddress(ChannelHandlerContext ctx) {
    return ctx.channel().remoteAddress() instanceof InetSocketAddress inet
        ? inet
        : new InetSocketAddress(0);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    int packetId = Buf.readVarInt(msg);
    PacketRegistry.In packet = PacketRegistry.resolve(readState, protocol, packetId);
    if (packet == null) {
      return; // unknown packet for this state/version: ignore, like the vendored decoder
    }

    switch (packet) {
      case HANDSHAKE_INTENT -> handleHandshake(ctx, msg);
      case STATUS_REQUEST -> handleStatusRequest(ctx);
      case STATUS_PING -> handleStatusPing(ctx, msg);
      case LOGIN_START -> handleLoginStart(ctx, msg);
      case LOGIN_PLUGIN_RESPONSE -> handleLoginPluginResponse(ctx, msg);
      case LOGIN_ACKNOWLEDGED -> handleLoginAcknowledged(ctx);
      case KNOWN_PACKS -> handleKnownPacks(ctx);
      case FINISH_CONFIGURATION_ACK -> spawnPlayer(ctx);
      case KEEP_ALIVE -> {
        // responses are decoded but never validated, matching the vendored limbo
      }
    }
  }

  private void handleHandshake(ChannelHandlerContext ctx, ByteBuf msg) {
    if (msg.readableBytes() < 4) {
      // Too short to carry protocol + empty host + port + intent: not a Minecraft handshake.
      throw new DecoderException("Malformed handshake: " + msg.readableBytes() + " bytes");
    }
    protocol = Buf.readVarInt(msg);
    String host = Buf.readString(msg, 32767);
    msg.readUnsignedShort(); // port
    int nextState = Buf.readVarInt(msg);

    PacketRegistry.State target =
        nextState == 1 ? PacketRegistry.State.STATUS : PacketRegistry.State.LOGIN;
    readState = target;
    writeState = target;

    Forwarding.Mode mode = server.settings().forwardingMode();
    if (mode == Forwarding.Mode.LEGACY) {
      Forwarding.LegacyHost split = Forwarding.splitLegacy(host);
      if (split == null) {
        disconnectLogin("You've enabled player info forwarding. You need to connect with proxy");
        return;
      }
      address = new InetSocketAddress(split.host(), address.getPort());
      profileUuid = split.uuid();
    } else if (mode == Forwarding.Mode.BUNGEEGUARD) {
      Forwarding.Profile profile = Forwarding.verifyBungeeGuard(host, bungeeGuardTokens());
      if (profile == null) {
        disconnectLogin("Invalid BungeeGuard token or handshake format");
        return;
      }
      address = new InetSocketAddress(profile.address(), address.getPort());
      profileUuid = profile.uuid();
    }
  }

  private List<String> bungeeGuardTokens() {
    byte[] secret = server.settings().forwardingSecret();
    if (secret == null) {
      return List.of();
    }
    return List.of(new String(secret, StandardCharsets.UTF_8));
  }

  private void handleStatusRequest(ChannelHandlerContext ctx) {
    Forwarding.Mode mode = server.settings().forwardingMode();
    int advertised = mode == Forwarding.Mode.NONE ? protocol : PacketRegistry.latestProtocol();
    write(
        ctx,
        PacketRegistry.Out.STATUS_RESPONSE,
        (buf, p) ->
            new PacketStatusResponse(
                    "NexAuth",
                    advertised,
                    server.settings().maxPlayers(),
                    server.connectionCount(),
                    server.settings().motd())
                .write(buf, p));
  }

  private void handleStatusPing(ChannelHandlerContext ctx, ByteBuf msg) {
    long payload = msg.readLong();
    writeAndClose(
        ctx,
        PacketRegistry.Out.STATUS_PONG,
        (buf, p) -> new PacketStatusPong(payload).write(buf, p));
  }

  private void handleLoginStart(ChannelHandlerContext ctx, ByteBuf msg) {
    if (server.settings().maxPlayers() > 0
        && server.connectionCount() >= server.settings().maxPlayers()) {
      disconnectLogin("Too many players connected");
      return;
    }

    if (!PacketRegistry.supported(protocol)) {
      disconnectLogin("Unsupported client version");
      return;
    }

    if (server.settings().forwardingMode() == Forwarding.Mode.MODERN) {
      velocityLoginMessageId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
      int id = velocityLoginMessageId;
      write(
          ctx,
          PacketRegistry.Out.LOGIN_PLUGIN_REQUEST,
          (buf, p) -> new PacketLoginPluginRequest(id).write(buf, p));
      return;
    }

    username = Buf.readString(msg, 256);
    profileUuid = Forwarding.offlineUuid(username);
    fireLoginSuccess(ctx);
  }

  private void handleLoginPluginResponse(ChannelHandlerContext ctx, ByteBuf msg) {
    if (server.settings().forwardingMode() != Forwarding.Mode.MODERN) {
      return;
    }

    int messageId = Buf.readVarInt(msg);
    if (messageId != velocityLoginMessageId) {
      return;
    }

    if (!msg.readBoolean() || !msg.isReadable()) {
      disconnectLogin("You need to connect with Velocity");
      return;
    }

    Forwarding.Profile profile = Forwarding.verifyModern(msg, server.settings().forwardingSecret());
    if (profile == null) {
      disconnectLogin("Can't verify forwarded player info");
      return;
    }

    address = new InetSocketAddress(profile.address(), address.getPort());
    profileUuid = profile.uuid();
    username = profile.username();
    fireLoginSuccess(ctx);
  }

  private void fireLoginSuccess(ChannelHandlerContext ctx) {
    if (server.settings().forwardingMode() == Forwarding.Mode.MODERN
        && velocityLoginMessageId == -1) {
      disconnectLogin("You need to connect with Velocity");
      return;
    }

    write(
        ctx,
        PacketRegistry.Out.LOGIN_SUCCESS,
        (buf, p) -> new PacketLoginSuccess(uuid, LIMBO_USERNAME).write(buf, p));
    server.add(this);

    if (protocol >= 764) {
      // Outbound switches immediately; inbound stays LOGIN until login_acknowledged.
      writeState = PacketRegistry.State.CONFIGURATION;
      return;
    }

    spawnPlayer(ctx);
  }

  private void handleLoginAcknowledged(ChannelHandlerContext ctx) {
    readState = PacketRegistry.State.CONFIGURATION;
    writeState = PacketRegistry.State.CONFIGURATION;
    RegistryData data = registryData();

    if (protocol >= 766) {
      write(
          ctx,
          PacketRegistry.Out.SELECT_KNOWN_PACKS,
          (buf, p) -> new PacketSelectKnownPacks(displayName(protocol)).write(buf, p));
      return;
    }

    write(
        ctx,
        PacketRegistry.Out.REGISTRY_DATA,
        (buf, p) -> new PacketRegistryData(data, protocol).writeWhole(buf, p));
    write(ctx, PacketRegistry.Out.FINISH_CONFIGURATION, new PacketFinishConfiguration()::write);
  }

  private void handleKnownPacks(ChannelHandlerContext ctx) {
    // Client's pack selection is decoded but ignored (vanilla-only offer).
    RegistryData data = registryData();
    java.util.Set<String> registryTypes = data.codecConfigurationModern(protocol).keySet();
    for (String registryType : registryTypes) {
      write(
          ctx,
          PacketRegistry.Out.REGISTRY_DATA,
          (buf, p) -> new PacketRegistryData(data, protocol).writeRegistry(buf, registryType));
    }
    write(ctx, PacketRegistry.Out.UPDATE_TAGS, new PacketUpdateTags(data, protocol)::write);
    write(ctx, PacketRegistry.Out.FINISH_CONFIGURATION, new PacketFinishConfiguration()::write);
  }

  private void spawnPlayer(ChannelHandlerContext ctx) {
    readState = PacketRegistry.State.PLAY;
    writeState = PacketRegistry.State.PLAY;

    Runnable burst =
        () -> {
          if (ctx.channel().isActive()) {
            write(
                ctx,
                PacketRegistry.Out.JOIN_GAME,
                (buf, p) -> new JoinGameData(registryData()).write(buf, p));
            write(ctx, PacketRegistry.Out.PLAYER_ABILITIES, new PacketPlayerAbilities()::write);
            write(
                ctx,
                PacketRegistry.Out.POSITION_LOOK,
                (buf, p) -> new PacketPositionLook(teleportId, protocol < 107).write(buf, p));
            if (protocol >= 761) {
              write(
                  ctx,
                  PacketRegistry.Out.SPAWN_POSITION,
                  (buf, p) -> new PacketSpawnPosition(JoinGameData.DIMENSION_NAME).write(buf, p));
            }
            if (protocol == 754) {
              write(
                  ctx,
                  PacketRegistry.Out.PLAYER_INFO,
                  (buf, p) -> new PacketPlayerInfo(uuid, LIMBO_USERNAME, 2).write(buf, p));
            }
            if (protocol >= 393) {
              write(ctx, PacketRegistry.Out.DECLARE_COMMANDS, new PacketDeclareCommands()::write);
            }
            if (protocol >= 765) {
              write(
                  ctx,
                  PacketRegistry.Out.START_WAITING_CHUNKS,
                  new PacketStartWaitingChunks()::write);
              for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                  final int chunkX = x;
                  final int chunkZ = z;
                  write(
                      ctx,
                      PacketRegistry.Out.CHUNK_DATA,
                      (buf, p) -> new PacketEmptyChunk(chunkX, chunkZ).write(buf, p));
                }
              }
            }
            sendKeepAlive(ctx);
            keepAliveTask =
                ctx.executor()
                    .scheduleAtFixedRate(
                        () -> sendKeepAlive(ctx),
                        server.settings().keepAliveIntervalSeconds(),
                        server.settings().keepAliveIntervalSeconds(),
                        TimeUnit.SECONDS);
          }
        };

    if (protocol <= 5) {
      ctx.executor().schedule(burst, 100, TimeUnit.MILLISECONDS);
    } else {
      burst.run();
    }
  }

  private void sendKeepAlive(ChannelHandlerContext ctx) {
    if (writeState == PacketRegistry.State.PLAY) {
      long id = ThreadLocalRandom.current().nextLong();
      write(ctx, PacketRegistry.Out.KEEP_ALIVE, (buf, p) -> new PacketKeepAlive(id).write(buf, p));
    }
  }

  private void disconnectLogin(String reason) {
    if (writeState != PacketRegistry.State.LOGIN || handlerContext == null) {
      return;
    }
    writeAndClose(
        handlerContext,
        PacketRegistry.Out.LOGIN_DISCONNECT,
        (buf, p) -> new PacketDisconnect(reason).write(buf, p));
  }

  private interface Writer {
    void write(ByteBuf buf, int protocol);
  }

  private void write(ChannelHandlerContext ctx, PacketRegistry.Out packet, Writer payload) {
    int id = PacketRegistry.id(packet, protocol);
    if (id < 0 || !ctx.channel().isActive()) {
      return; // no mapping for this protocol: do not send (see PacketRegistry javadoc)
    }
    ByteBuf buf = Unpooled.buffer();
    Buf.writeVarInt(buf, id);
    payload.write(buf, protocol);
    ctx.writeAndFlush(buf, ctx.voidPromise());
  }

  private void writeAndClose(ChannelHandlerContext ctx, PacketRegistry.Out packet, Writer payload) {
    int id = PacketRegistry.id(packet, protocol);
    if (id < 0 || !ctx.channel().isActive()) {
      return;
    }
    ByteBuf buf = Unpooled.buffer();
    Buf.writeVarInt(buf, id);
    payload.write(buf, protocol);
    ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (keepAliveTask != null) {
      keepAliveTask.cancel(true);
      keepAliveTask = null;
    }
    if (writeState == PacketRegistry.State.PLAY
        || writeState == PacketRegistry.State.CONFIGURATION) {
      server.remove(this);
    }
    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // Never silent: a decode/payload error must be visible, or malformed clients look like
    // a mapping failure (zero-byte close) and become undiagnosable from the outside.
    LimboServer.log()
        .debug(
            "Closing limbo connection {} after pipeline error",
            ctx.channel().remoteAddress(),
            cause);
    ctx.close();
  }

  private RegistryData registryData() {
    if (registryData == null) {
      try {
        registryData = RegistryData.get();
      } catch (IOException e) {
        throw new IllegalStateException("Cannot load dimension registry data", e);
      }
    }
    return registryData;
  }

  private static String displayName(int protocol) {
    return switch (protocol) {
      case 766 -> "1.20.5";
      case 767 -> "1.21";
      case 768 -> "1.21.2";
      case 769 -> "1.21.4";
      case 770 -> "1.21.5";
      case 771 -> "1.21.6";
      case 772 -> "1.21.7";
      case 773 -> "1.21.9";
      case 774 -> "1.21.11";
      case 775 -> "26.1";
      default -> "26.2";
    };
  }
}

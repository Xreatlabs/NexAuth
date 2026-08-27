/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

/**
 * Single seam between the limbo and the packetevents-api per-version packet id tables.
 *
 * <p>The packetevents jar is used strictly as a static mapping library: {@link
 * PacketType#prepare()} loads the tables once in the static initializer and every lookup goes
 * through {@link PacketTypeCommon#getId(ClientVersion)}. Never call {@code PacketEvents.getAPI()}
 * or {@code PacketEvents.setAPI()} here — the Paper platform runs its own PacketEvents instance and
 * this code must stay invisible to it.
 *
 * <p><b>-1 semantics:</b> {@link #id(Out, int)} returns {@code -1} when a packet has no mapping for
 * the given protocol. Callers must treat {@code -1} as <i>do not send</i>. Known omissions:
 *
 * <ul>
 *   <li>All configuration-state packets below protocol 764 (1.20.2): the state does not exist
 *       there. packetevents would otherwise silently answer with its oldest configuration table,
 *       which is garbage for those versions, so this class forces {@code -1}.
 *   <li>{@link Out#SELECT_KNOWN_PACKS} and {@link In#KNOWN_PACKS} below protocol 766 (1.20.5):
 *       known packs do not exist yet (packetevents itself reports {@code -1} there).
 *   <li>{@link Out#DECLARE_COMMANDS} below protocol 393 (1.13) and at protocols packetevents has no
 *       exact mapping for: the constant exists in the mapping tables only from 1.13 on. Clients
 *       tolerate the packet's absence.
 *   <li>{@link Out#PLAYER_INFO} from protocol 761 (1.19.3) on: the packet was split into add/remove
 *       packets there and packetevents reports {@code -1} for the old constant.
 * </ul>
 *
 * <p>Any protocol between {@link #MIN_PROTOCOL} and packetevents' latest known protocol is
 * accepted; packetevents resolves unlisted in-range protocols to the nearest known mapping.
 * Protocols outside the range are unsupported.
 */
public final class PacketRegistry {

  /** Oldest protocol (1.7.2) the limbo can serve. */
  public static final int MIN_PROTOCOL = 4;

  /** First protocol (1.20.2) that has the configuration state. */
  public static final int CONFIGURATION_PROTOCOL = 764;

  static {
    PacketType.prepare();
  }

  private PacketRegistry() {}

  /** Protocol connection states the limbo drives. */
  public enum State {
    HANDSHAKE,
    STATUS,
    LOGIN,
    CONFIGURATION,
    PLAY
  }

  /** Clientbound packets the limbo sends, by semantic name. */
  public enum Out {
    STATUS_RESPONSE(State.STATUS, PacketType.Status.Server.RESPONSE),
    STATUS_PONG(State.STATUS, PacketType.Status.Server.PONG),
    LOGIN_DISCONNECT(State.LOGIN, PacketType.Login.Server.DISCONNECT),
    LOGIN_PLUGIN_REQUEST(State.LOGIN, PacketType.Login.Server.LOGIN_PLUGIN_REQUEST),
    LOGIN_SUCCESS(State.LOGIN, PacketType.Login.Server.LOGIN_SUCCESS),
    CONFIGURATION_DISCONNECT(State.CONFIGURATION, PacketType.Configuration.Server.DISCONNECT),
    PLAY_DISCONNECT(State.PLAY, PacketType.Play.Server.DISCONNECT),
    JOIN_GAME(State.PLAY, PacketType.Play.Server.JOIN_GAME),
    KEEP_ALIVE(State.PLAY, PacketType.Play.Server.KEEP_ALIVE),
    PLAYER_ABILITIES(State.PLAY, PacketType.Play.Server.PLAYER_ABILITIES),
    POSITION_LOOK(State.PLAY, PacketType.Play.Server.PLAYER_POSITION_AND_LOOK),
    SPAWN_POSITION(State.PLAY, PacketType.Play.Server.SPAWN_POSITION),
    START_WAITING_CHUNKS(State.PLAY, PacketType.Play.Server.CHANGE_GAME_STATE),
    SELECT_KNOWN_PACKS(State.CONFIGURATION, PacketType.Configuration.Server.SELECT_KNOWN_PACKS),
    REGISTRY_DATA(State.CONFIGURATION, PacketType.Configuration.Server.REGISTRY_DATA),
    UPDATE_TAGS(State.CONFIGURATION, PacketType.Configuration.Server.UPDATE_TAGS),
    FINISH_CONFIGURATION(State.CONFIGURATION, PacketType.Configuration.Server.CONFIGURATION_END),
    DECLARE_COMMANDS(State.PLAY, PacketType.Play.Server.DECLARE_COMMANDS),
    PLAYER_INFO(State.PLAY, PacketType.Play.Server.PLAYER_INFO),
    CHUNK_DATA(State.PLAY, PacketType.Play.Server.CHUNK_DATA);

    private final State state;
    private final PacketTypeCommon type;

    Out(State state, PacketTypeCommon type) {
      this.state = state;
      this.type = type;
    }

    /** State this packet is sent in. */
    public State state() {
      return state;
    }
  }

  /** Serverbound packets the limbo reads, by semantic name. */
  public enum In {
    HANDSHAKE_INTENT(new Mapping(State.HANDSHAKE, PacketType.Handshaking.Client.HANDSHAKE)),
    STATUS_REQUEST(new Mapping(State.STATUS, PacketType.Status.Client.REQUEST)),
    STATUS_PING(new Mapping(State.STATUS, PacketType.Status.Client.PING)),
    LOGIN_START(new Mapping(State.LOGIN, PacketType.Login.Client.LOGIN_START)),
    LOGIN_PLUGIN_RESPONSE(new Mapping(State.LOGIN, PacketType.Login.Client.LOGIN_PLUGIN_RESPONSE)),
    LOGIN_ACKNOWLEDGED(new Mapping(State.LOGIN, PacketType.Login.Client.LOGIN_SUCCESS_ACK)),
    KNOWN_PACKS(
        new Mapping(State.CONFIGURATION, PacketType.Configuration.Client.SELECT_KNOWN_PACKS)),
    FINISH_CONFIGURATION_ACK(
        new Mapping(State.CONFIGURATION, PacketType.Configuration.Client.CONFIGURATION_END_ACK)),
    KEEP_ALIVE(
        new Mapping(State.PLAY, PacketType.Play.Client.KEEP_ALIVE),
        new Mapping(State.CONFIGURATION, PacketType.Configuration.Client.KEEP_ALIVE));

    private final Mapping[] mappings;

    In(Mapping... mappings) {
      this.mappings = mappings;
    }
  }

  private record Mapping(State state, PacketTypeCommon type) {}

  /**
   * Clientbound packet id for the protocol, or {@code -1} when the packet has no mapping there (see
   * class javadoc for the documented omissions — callers must not send).
   *
   * <p>Handshake, login and status ids are protocol-invariant across the entire supported range
   * (verified against the mapping tables), and {@code ClientVersion.getById} clamps unknown
   * protocols instead of failing — so those states are never gated on {@link #supported(int)}. This
   * keeps the login-state disconnects deliverable to unknown-protocol clients, which is how the
   * vendored limbo disconnected them cleanly.
   */
  public static int id(Out packet, int protocol) {
    if (packet.state == State.CONFIGURATION || packet.state == State.PLAY) {
      if (!supported(protocol)) {
        return -1;
      }
      if (packet.state == State.CONFIGURATION && protocol < CONFIGURATION_PROTOCOL) {
        return -1;
      }
    }
    return packet.type.getId(ClientVersion.getById(protocol));
  }

  /**
   * Resolves a serverbound raw packet id to its semantic packet in the given state, or {@code null}
   * when nothing matches. The handshake is version-free (packet id 0 in every protocol ever) and is
   * answered before the connection's protocol is even known, so it is never gated on {@link
   * #supported(int)}. Configuration-state packets never match below protocol 764.
   */
  public static In resolve(State state, int protocol, int rawId) {
    if (state == State.HANDSHAKE) {
      return rawId == 0 ? In.HANDSHAKE_INTENT : null;
    }
    // Status and login ids are protocol-invariant; unknown-protocol clients must still be
    // readable so the limbo can disconnect them cleanly with "Unsupported client version".
    if ((state == State.CONFIGURATION || state == State.PLAY) && !supported(protocol)) {
      return null;
    }
    for (In packet : In.values()) {
      for (Mapping mapping : packet.mappings) {
        if (mapping.state() != state) {
          continue;
        }
        if (state == State.CONFIGURATION && protocol < CONFIGURATION_PROTOCOL) {
          continue;
        }
        int id = mapping.type().getId(ClientVersion.getById(protocol));
        if (id >= 0 && id == rawId) {
          return packet;
        }
      }
    }
    return null;
  }

  /** Whether the limbo can serve the protocol at all. */
  public static boolean supported(int protocol) {
    return protocol >= MIN_PROTOCOL && protocol <= latestProtocol();
  }

  /**
   * Newest protocol known to the mapping tables; advertised in status pings when forwarding is
   * enabled (the vendored limbo's {@code Version.getMax()}).
   */
  public static int latestProtocol() {
    return ClientVersion.getLatest().getProtocolVersion();
  }

  /**
   * Primary state the serverbound packet is received in. {@link In#KEEP_ALIVE} is additionally
   * valid in {@link State#CONFIGURATION}; use {@link #resolve(State, int, int)} for per-state
   * matching.
   */
  public static State stateOf(In packet) {
    return packet.mappings[0].state();
  }
}

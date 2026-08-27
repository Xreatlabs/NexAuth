/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import static org.junit.jupiter.api.Assertions.*;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PacketRegistryTest {

  @BeforeAll
  static void prepareMappings() {
    PacketType.prepare();
  }

  /** Every protocol version packetevents knows, straight from the mapping library. */
  private static List<Integer> supportedProtocols() {
    var protocols = new ArrayList<Integer>();
    for (ClientVersion version : ClientVersion.values()) {
      int protocol = version.getProtocolVersion();
      if (protocol > 0 && PacketRegistry.supported(protocol)) {
        protocols.add(protocol);
      }
    }
    return protocols;
  }

  @Test
  void supportedRejectsProtocolsOutsideTheMappingRange() {
    assertTrue(PacketRegistry.supported(766));
    assertTrue(PacketRegistry.supported(PacketRegistry.MIN_PROTOCOL));
    assertFalse(PacketRegistry.supported(3));
    assertFalse(PacketRegistry.supported(0));
    assertFalse(PacketRegistry.supported(-1));
    assertFalse(PacketRegistry.supported(ClientVersion.getLatest().getProtocolVersion() + 1));
  }

  @Test
  void everyOutboundIdResolvesExceptDocumentedOmissions() {
    for (int protocol : supportedProtocols()) {
      for (PacketRegistry.Out packet : PacketRegistry.Out.values()) {
        int id = PacketRegistry.id(packet, protocol);
        assertTrue(
            id >= 0 || isDocumentedOmission(packet, protocol),
            packet
                + " is negative ("
                + id
                + ") at protocol "
                + protocol
                + " without a documented omission");
      }
    }
  }

  @Test
  void everyInboundPacketResolvesInItsOwnState() {
    for (int protocol : supportedProtocols()) {
      for (PacketRegistry.In packet : PacketRegistry.In.values()) {
        PacketRegistry.State state = PacketRegistry.stateOf(packet);
        if (state == PacketRegistry.State.CONFIGURATION
            && protocol < PacketRegistry.CONFIGURATION_PROTOCOL) {
          continue;
        }
        if (packet == PacketRegistry.In.KNOWN_PACKS
            && PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, protocol) == -1) {
          continue;
        }
        int id = inboundId(packet, state, protocol);
        assertTrue(id >= 0, packet + " has no id in " + state + " at protocol " + protocol);
        assertEquals(
            packet,
            PacketRegistry.resolve(state, protocol, id),
            packet + " does not round-trip at protocol " + protocol);
      }
    }
  }

  @Test
  void configurationPacketsDoNotExistBelow1202() {
    for (int protocol : List.of(4, 393, 735, 754, 763)) {
      for (PacketRegistry.Out packet :
          List.of(
              PacketRegistry.Out.CONFIGURATION_DISCONNECT,
              PacketRegistry.Out.REGISTRY_DATA,
              PacketRegistry.Out.FINISH_CONFIGURATION,
              PacketRegistry.Out.SELECT_KNOWN_PACKS)) {
        assertEquals(
            -1,
            PacketRegistry.id(packet, protocol),
            packet + " must not map at protocol " + protocol);
      }
      assertNull(PacketRegistry.resolve(PacketRegistry.State.CONFIGURATION, protocol, 0));
      assertNull(PacketRegistry.resolve(PacketRegistry.State.CONFIGURATION, protocol, 2));
    }
  }

  @Test
  void knownPacksStartsAt1205() {
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, 764));
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, 765));
    assertEquals(14, PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, 766));
  }

  @Test
  void declareCommandsExistsOnlyWhereMapped() {
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 4));
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 47));
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 340));
    assertEquals(17, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 393));
    assertEquals(16, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 754));
    assertTrue(PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 766) >= 0);
  }

  @Test
  void playerInfoVanishesAt1193() {
    assertTrue(PacketRegistry.id(PacketRegistry.Out.PLAYER_INFO, 754) >= 0);
    assertTrue(PacketRegistry.id(PacketRegistry.Out.PLAYER_INFO, 760) >= 0);
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.PLAYER_INFO, 761));
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.PLAYER_INFO, 766));
    assertEquals(-1, PacketRegistry.id(PacketRegistry.Out.PLAYER_INFO, 776));
  }

  @Test
  void stateOfMatchesThePacketRegistryStates() {
    assertEquals(
        PacketRegistry.State.HANDSHAKE, PacketRegistry.stateOf(PacketRegistry.In.HANDSHAKE_INTENT));
    assertEquals(PacketRegistry.State.LOGIN, PacketRegistry.stateOf(PacketRegistry.In.LOGIN_START));
    assertEquals(
        PacketRegistry.State.CONFIGURATION, PacketRegistry.stateOf(PacketRegistry.In.KNOWN_PACKS));
    assertEquals(PacketRegistry.State.PLAY, PacketRegistry.stateOf(PacketRegistry.In.KEEP_ALIVE));
  }

  @Test
  void resolveReturnsNullForUnknownAndCrossStateIds() {
    assertNull(PacketRegistry.resolve(PacketRegistry.State.LOGIN, 766, 0x7F));
    assertNull(PacketRegistry.resolve(PacketRegistry.State.PLAY, 3, 0));
    assertEquals(
        PacketRegistry.In.LOGIN_START, PacketRegistry.resolve(PacketRegistry.State.LOGIN, 766, 0));
  }

  /** The only packets allowed to be {@code -1}, per the PacketRegistry javadoc. */
  private static boolean isDocumentedOmission(PacketRegistry.Out packet, int protocol) {
    if (packet.state() == PacketRegistry.State.CONFIGURATION
        && protocol < PacketRegistry.CONFIGURATION_PROTOCOL) {
      return true;
    }
    if (packet == PacketRegistry.Out.SELECT_KNOWN_PACKS && protocol < 766) {
      return true;
    }
    if (packet == PacketRegistry.Out.PLAYER_INFO && protocol >= 761) {
      return true;
    }
    return packet == PacketRegistry.Out.DECLARE_COMMANDS
        && PacketRegistry.id(packet, protocol) == -1
        && protocol < 393;
  }

  private static int inboundId(PacketRegistry.In packet, PacketRegistry.State state, int protocol) {
    for (int raw = 0; raw < 128; raw++) {
      if (PacketRegistry.resolve(state, protocol, raw) == packet) {
        return raw;
      }
    }
    return -1;
  }

  // --- pinned ids, verbatim from the packetevents 2.13.0 probe ---

  @Test
  void pinsProbeReportIdsVerbatim() {
    // protocol-invariant login/status ids
    for (int protocol : List.of(735, 754, 764, 766, 776)) {
      assertEquals(0, PacketRegistry.id(PacketRegistry.Out.STATUS_RESPONSE, protocol));
      assertEquals(1, PacketRegistry.id(PacketRegistry.Out.STATUS_PONG, protocol));
      assertEquals(0, PacketRegistry.id(PacketRegistry.Out.LOGIN_DISCONNECT, protocol));
      assertEquals(4, PacketRegistry.id(PacketRegistry.Out.LOGIN_PLUGIN_REQUEST, protocol));
      assertEquals(2, PacketRegistry.id(PacketRegistry.Out.LOGIN_SUCCESS, protocol));
      assertEquals(
          PacketRegistry.In.LOGIN_START,
          PacketRegistry.resolve(PacketRegistry.State.LOGIN, protocol, 0));
      assertEquals(
          PacketRegistry.In.LOGIN_PLUGIN_RESPONSE,
          PacketRegistry.resolve(PacketRegistry.State.LOGIN, protocol, 2));
      assertEquals(
          PacketRegistry.In.LOGIN_ACKNOWLEDGED,
          PacketRegistry.resolve(PacketRegistry.State.LOGIN, protocol, 3));
    }

    // play ids per protocol
    assertEquals(37, PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, 735));
    assertEquals(36, PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, 754));
    assertEquals(41, PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, 764));
    assertEquals(43, PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, 766));
    assertEquals(43, PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, 767));
    assertEquals(49, PacketRegistry.id(PacketRegistry.Out.JOIN_GAME, 776));

    assertEquals(32, PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, 735));
    assertEquals(31, PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, 754));
    assertEquals(36, PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, 764));
    assertEquals(38, PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, 766));
    assertEquals(38, PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, 767));
    assertEquals(44, PacketRegistry.id(PacketRegistry.Out.KEEP_ALIVE, 776));

    assertEquals(26, PacketRegistry.id(PacketRegistry.Out.PLAY_DISCONNECT, 735));
    assertEquals(25, PacketRegistry.id(PacketRegistry.Out.PLAY_DISCONNECT, 754));
    assertEquals(27, PacketRegistry.id(PacketRegistry.Out.PLAY_DISCONNECT, 764));
    assertEquals(29, PacketRegistry.id(PacketRegistry.Out.PLAY_DISCONNECT, 766));
    assertEquals(32, PacketRegistry.id(PacketRegistry.Out.PLAY_DISCONNECT, 776));

    assertEquals(53, PacketRegistry.id(PacketRegistry.Out.POSITION_LOOK, 735));
    assertEquals(52, PacketRegistry.id(PacketRegistry.Out.POSITION_LOOK, 754));
    assertEquals(62, PacketRegistry.id(PacketRegistry.Out.POSITION_LOOK, 764));
    assertEquals(64, PacketRegistry.id(PacketRegistry.Out.POSITION_LOOK, 766));
    assertEquals(64, PacketRegistry.id(PacketRegistry.Out.POSITION_LOOK, 767));
    assertEquals(72, PacketRegistry.id(PacketRegistry.Out.POSITION_LOOK, 776));

    assertEquals(49, PacketRegistry.id(PacketRegistry.Out.PLAYER_ABILITIES, 735));
    assertEquals(48, PacketRegistry.id(PacketRegistry.Out.PLAYER_ABILITIES, 754));
    assertEquals(54, PacketRegistry.id(PacketRegistry.Out.PLAYER_ABILITIES, 764));
    assertEquals(56, PacketRegistry.id(PacketRegistry.Out.PLAYER_ABILITIES, 766));
    assertEquals(56, PacketRegistry.id(PacketRegistry.Out.PLAYER_ABILITIES, 767));
    assertEquals(64, PacketRegistry.id(PacketRegistry.Out.PLAYER_ABILITIES, 776));

    assertEquals(77, PacketRegistry.id(PacketRegistry.Out.SPAWN_POSITION, 735));
    assertEquals(66, PacketRegistry.id(PacketRegistry.Out.SPAWN_POSITION, 754));
    assertEquals(82, PacketRegistry.id(PacketRegistry.Out.SPAWN_POSITION, 764));
    assertEquals(86, PacketRegistry.id(PacketRegistry.Out.SPAWN_POSITION, 766));
    assertEquals(86, PacketRegistry.id(PacketRegistry.Out.SPAWN_POSITION, 767));
    assertEquals(97, PacketRegistry.id(PacketRegistry.Out.SPAWN_POSITION, 776));

    assertEquals(17, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 735));
    assertEquals(16, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 754));
    assertEquals(17, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 764));
    assertEquals(17, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 766));
    assertEquals(17, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 767));
    assertEquals(16, PacketRegistry.id(PacketRegistry.Out.DECLARE_COMMANDS, 776));

    // configuration ids, only pinned for >= 766 per the probe
    assertEquals(14, PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, 766));
    assertEquals(14, PacketRegistry.id(PacketRegistry.Out.SELECT_KNOWN_PACKS, 776));
    assertEquals(7, PacketRegistry.id(PacketRegistry.Out.REGISTRY_DATA, 766));
    assertEquals(7, PacketRegistry.id(PacketRegistry.Out.REGISTRY_DATA, 776));
    assertEquals(3, PacketRegistry.id(PacketRegistry.Out.FINISH_CONFIGURATION, 766));
    assertEquals(3, PacketRegistry.id(PacketRegistry.Out.FINISH_CONFIGURATION, 776));
    assertEquals(2, PacketRegistry.id(PacketRegistry.Out.CONFIGURATION_DISCONNECT, 766));
    assertEquals(2, PacketRegistry.id(PacketRegistry.Out.CONFIGURATION_DISCONNECT, 776));

    assertEquals(
        PacketRegistry.In.KNOWN_PACKS,
        PacketRegistry.resolve(PacketRegistry.State.CONFIGURATION, 766, 7));
    assertEquals(
        PacketRegistry.In.FINISH_CONFIGURATION_ACK,
        PacketRegistry.resolve(PacketRegistry.State.CONFIGURATION, 766, 3));
    assertEquals(
        PacketRegistry.In.KEEP_ALIVE,
        PacketRegistry.resolve(PacketRegistry.State.CONFIGURATION, 766, 4));
    assertEquals(
        PacketRegistry.In.KEEP_ALIVE, PacketRegistry.resolve(PacketRegistry.State.PLAY, 766, 24));
    assertEquals(
        PacketRegistry.In.KEEP_ALIVE, PacketRegistry.resolve(PacketRegistry.State.PLAY, 776, 28));
    assertEquals(
        PacketRegistry.In.KEEP_ALIVE, PacketRegistry.resolve(PacketRegistry.State.PLAY, 735, 16));
    assertEquals(
        PacketRegistry.In.KEEP_ALIVE, PacketRegistry.resolve(PacketRegistry.State.PLAY, 754, 16));
    assertEquals(
        PacketRegistry.In.KEEP_ALIVE, PacketRegistry.resolve(PacketRegistry.State.PLAY, 764, 20));
  }
}

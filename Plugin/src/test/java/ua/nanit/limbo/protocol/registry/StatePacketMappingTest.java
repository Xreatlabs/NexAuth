package ua.nanit.limbo.protocol.registry;

import org.junit.jupiter.api.Test;
import ua.nanit.limbo.protocol.packets.play.PacketBossBar;
import ua.nanit.limbo.protocol.packets.play.PacketChatMessage;
import ua.nanit.limbo.protocol.packets.play.PacketDeclareCommands;
import ua.nanit.limbo.protocol.packets.play.PacketEmptyChunk;
import ua.nanit.limbo.protocol.packets.play.PacketGameEvent;
import ua.nanit.limbo.protocol.packets.play.PacketJoinGame;
import ua.nanit.limbo.protocol.packets.play.PacketKeepAlive;
import ua.nanit.limbo.protocol.packets.play.PacketPlayerAbilities;
import ua.nanit.limbo.protocol.packets.play.PacketPlayerInfo;
import ua.nanit.limbo.protocol.packets.play.PacketPlayerListHeader;
import ua.nanit.limbo.protocol.packets.play.PacketPlayerPositionAndLook;
import ua.nanit.limbo.protocol.packets.play.PacketPluginMessage;
import ua.nanit.limbo.protocol.packets.play.PacketSpawnPosition;
import ua.nanit.limbo.protocol.packets.play.PacketTitleSetSubTitle;
import ua.nanit.limbo.protocol.packets.play.PacketTitleSetTitle;
import ua.nanit.limbo.protocol.packets.play.PacketTitleTimes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatePacketMappingTest {

    @Test
    void mapsLatestKnownPlayClientboundPackets() {
        assertPlayMapping(Version.V1_21_9);
        assertPlayMapping(Version.V1_21_11);
        assertPlayMapping(Version.V26_1);
    }

    private static void assertPlayMapping(Version version) {
        State.PacketRegistry registry = State.PLAY.clientBound.getRegistry(version);

        assertEquals(0x09, registry.getPacketId(PacketBossBar.class));
        assertEquals(0x10, registry.getPacketId(PacketDeclareCommands.class));
        assertEquals(0x18, registry.getPacketId(PacketPluginMessage.class));
        assertEquals(0x26, registry.getPacketId(PacketGameEvent.class));
        assertEquals(0x2B, registry.getPacketId(PacketKeepAlive.class));
        assertEquals(0x2C, registry.getPacketId(PacketEmptyChunk.class));
        assertEquals(0x30, registry.getPacketId(PacketJoinGame.class));
        assertEquals(0x3E, registry.getPacketId(PacketPlayerAbilities.class));
        assertEquals(0x44, registry.getPacketId(PacketPlayerInfo.class));
        assertEquals(0x46, registry.getPacketId(PacketPlayerPositionAndLook.class));
        assertEquals(0x5F, registry.getPacketId(PacketSpawnPosition.class));
        assertEquals(0x6E, registry.getPacketId(PacketTitleSetSubTitle.class));
        assertEquals(0x70, registry.getPacketId(PacketTitleSetTitle.class));
        assertEquals(0x71, registry.getPacketId(PacketTitleTimes.class));
        assertEquals(0x77, registry.getPacketId(PacketChatMessage.class));
        assertEquals(0x78, registry.getPacketId(PacketPlayerListHeader.class));
    }
}

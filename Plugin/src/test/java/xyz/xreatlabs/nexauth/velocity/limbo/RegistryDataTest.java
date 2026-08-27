/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.Test;

class RegistryDataTest {

  @Test
  void everyLegacyCodecResourceLoadsAndContainsDimensionType() throws IOException {
    RegistryData data = RegistryData.get();

    // 735-736 (1.16-1.16.1) codecs are the pre-1.16.2 shape: a top-level "dimension" list tag
    // of nameless elements; 751+ use the namespaced minecraft:dimension_type registry shape.
    for (int protocol : new int[] {735, 736}) {
      CompoundBinaryTag codec = data.codecPlay(protocol);
      assertNotNull(codec, "codec must load for protocol " + protocol);
      assertTrue(
          codec.keySet().contains("dimension"),
          "1.16 codec must contain the legacy dimension list, got " + codec.keySet());
      assertTrue(codec.getList("dimension").size() > 0, "dimension list must be non-empty");
    }

    int[] namespaced = {751, 753, 754, 755, 756, 757, 758, 759, 760, 761, 762, 763, 764, 765};
    for (int protocol : namespaced) {
      CompoundBinaryTag codec = data.codecPlay(protocol);
      assertTrue(
          codec.keySet().contains("minecraft:dimension_type"),
          "codec for protocol " + protocol + " must contain minecraft:dimension_type");
    }
  }

  @Test
  void configurationLegacyUsesCodec120() throws IOException {
    RegistryData data = RegistryData.get();

    CompoundBinaryTag codec = data.codecConfigurationLegacy(764);
    assertTrue(codec.keySet().contains("minecraft:dimension_type"));
  }

  @Test
  void protocol760UsesThe1_19_1Codec() throws IOException {
    RegistryData data = RegistryData.get();

    // 1.19.1/1.19.2 (760) reworked minecraft:chat_type; feeding them codec_1_19 gets rejected.
    assertNotSame(data.codecPlay(759), data.codecPlay(760), "760 must not reuse the 1.19 codec");
    assertSame(data.codecPlay(760), data.codecPlay(761), "760 and 761 share codec_1_19_1");

    ListBinaryTag chatTypes =
        data.codecPlay(760).getCompound("minecraft:chat_type").getList("value");
    boolean hasMsgCommandIncoming = false;
    for (int i = 0; i < chatTypes.size(); i++) {
      if (chatTypes.getCompound(i).getString("name").equals("minecraft:msg_command_incoming")) {
        hasMsgCommandIncoming = true;
        break;
      }
    }
    assertTrue(
        hasMsgCommandIncoming,
        "codec_1_19_1 chat_type must split msg_command into incoming/outgoing entries");
  }

  @Test
  void configurationModernSelectsPerBracket() throws IOException {
    RegistryData data = RegistryData.get();

    // Each ≥766 protocol must resolve to a codec with dimension_type; spot-check that the
    // selection brackets differ where the vendored table differs (766 vs 772).
    CompoundBinaryTag for766 = data.codecConfigurationModern(766);
    CompoundBinaryTag for772 = data.codecConfigurationModern(772);

    assertTrue(for766.keySet().contains("minecraft:dimension_type"));
    assertTrue(for772.keySet().contains("minecraft:dimension_type"));
    assertNotSame(for766, for772, "1.20.5 and 1.21.7 must use different codecs");
  }

  @Test
  void dimensionIdFallsInTheEndForModernProtocols() throws IOException {
    RegistryData data = RegistryData.get();

    for (int protocol : new int[] {766, 767, 768, 769, 770, 771, 772, 773, 774, 775, 776}) {
      int id = data.dimensionId(protocol, JoinGameData.DIMENSION_NAME);
      assertTrue(id >= 0, "dimension id must be non-negative for protocol " + protocol);
    }
  }

  @Test
  void dimensionElementResolvesTheEnd() throws IOException {
    RegistryData data = RegistryData.get();

    CompoundBinaryTag element = data.dimensionElement(758, JoinGameData.DIMENSION_NAME);
    assertNotNull(element);
  }

  @Test
  void tagsWriteProducesBytesForEveryBracket() throws IOException {
    RegistryData data = RegistryData.get();

    for (int protocol : new int[] {766, 767, 768, 769, 770, 771, 772}) {
      ByteBuf buf = Unpooled.buffer();
      try {
        data.writeTags(buf, data.tags(protocol));
        assertTrue(buf.readableBytes() > 0, "tags wire data must be non-empty for " + protocol);
      } finally {
        buf.release();
      }
    }
  }

  @Test
  void registryEntriesExposeNamesAndElements() throws IOException {
    RegistryData data = RegistryData.get();
    CompoundBinaryTag codec = data.codecConfigurationModern(766);

    var entries = data.registryEntries(codec, "minecraft:dimension_type");
    assertFalse(entries.isEmpty());
    for (var entry : entries) {
      assertNotNull(entry.name());
      assertTrue(entry.name().startsWith("minecraft:"), "entry names must be namespaced");
    }
  }

  @Test
  void dimensionTypeEntriesAreListShaped() throws IOException {
    RegistryData data = RegistryData.get();
    CompoundBinaryTag codec = data.codecConfigurationModern(766);

    ListBinaryTag values = codec.getCompound("minecraft:dimension_type").getList("value");
    assertTrue(values.size() > 0, "dimension_type value list must be non-empty");
  }
}

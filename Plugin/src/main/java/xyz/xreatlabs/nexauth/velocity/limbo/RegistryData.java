/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

/**
 * Dimension/registry NBT payloads, loaded once from {@code /dimension/} classpath resources (GZIP,
 * big-endian) and selected per protocol. Version brackets port the vendored limbo's {@code
 * DimensionRegistry} + {@code PacketSnapshots} selection tables.
 */
public final class RegistryData {

  /** A single registry entry: name plus optional element NBT. */
  public record Entry(String name, CompoundBinaryTag element) {}

  private static volatile RegistryData instance;

  private final CompoundBinaryTag codec1_16;
  private final CompoundBinaryTag codec1_16_2;
  private final CompoundBinaryTag codec1_17;
  private final CompoundBinaryTag codec1_18_2;
  private final CompoundBinaryTag codec1_19;
  private final CompoundBinaryTag codec1_19_1;
  private final CompoundBinaryTag codec1_19_4;
  private final CompoundBinaryTag codec1_20;
  private final CompoundBinaryTag codec1_20_5;
  private final CompoundBinaryTag codec1_21;
  private final CompoundBinaryTag codec1_21_2;
  private final CompoundBinaryTag codec1_21_4;
  private final CompoundBinaryTag codec1_21_5;
  private final CompoundBinaryTag codec1_21_6;
  private final CompoundBinaryTag codec1_21_7;

  private final CompoundBinaryTag tags1_20_5;
  private final CompoundBinaryTag tags1_21;
  private final CompoundBinaryTag tags1_21_2;
  private final CompoundBinaryTag tags1_21_4;
  private final CompoundBinaryTag tags1_21_5;
  private final CompoundBinaryTag tags1_21_6;
  private final CompoundBinaryTag tags1_21_7;

  private RegistryData() throws IOException {
    codec1_16 = read("codec_1_16.nbt");
    codec1_16_2 = read("codec_1_16_2.nbt");
    codec1_17 = read("codec_1_17.nbt");
    codec1_18_2 = read("codec_1_18_2.nbt");
    codec1_19 = read("codec_1_19.nbt");
    codec1_19_1 = read("codec_1_19_1.nbt");
    codec1_19_4 = read("codec_1_19_4.nbt");
    codec1_20 = read("codec_1_20.nbt");
    codec1_20_5 = read("codec_1_20_5.nbt");
    codec1_21 = read("codec_1_21.nbt");
    codec1_21_2 = read("codec_1_21_2.nbt");
    codec1_21_4 = read("codec_1_21_4.nbt");
    codec1_21_5 = read("codec_1_21_5.nbt");
    codec1_21_6 = read("codec_1_21_6.nbt");
    codec1_21_7 = read("codec_1_21_7.nbt");

    tags1_20_5 = read("tags_1_20_5.nbt");
    tags1_21 = read("tags_1_21.nbt");
    tags1_21_2 = read("tags_1_21_2.nbt");
    tags1_21_4 = read("tags_1_21_4.nbt");
    tags1_21_5 = read("tags_1_21_5.nbt");
    tags1_21_6 = read("tags_1_21_6.nbt");
    tags1_21_7 = read("tags_1_21_7.nbt");
  }

  /** Loads all resources once; safe to call repeatedly. */
  public static RegistryData get() throws IOException {
    var local = instance;
    if (local == null) {
      synchronized (RegistryData.class) {
        if (instance == null) {
          instance = new RegistryData();
        }
        local = instance;
      }
    }
    return local;
  }

  /**
   * Configuration-state registry codec for protocols 764-765 (1.20.2-1.20.4): single verbatim
   * codec_1_20 NBT.
   */
  public CompoundBinaryTag codecConfigurationLegacy(int protocol) {
    return codec1_20;
  }

  /** Configuration-state per-registry codec for protocols ≥766 (known-packs path). */
  public CompoundBinaryTag codecConfigurationModern(int protocol) {
    if (protocol >= 772) {
      return codec1_21_7;
    }
    if (protocol >= 771) {
      return codec1_21_6;
    }
    if (protocol >= 770) {
      return codec1_21_5;
    }
    if (protocol >= 769) {
      return codec1_21_4;
    }
    if (protocol >= 768) {
      return codec1_21_2;
    }
    if (protocol >= 767) {
      return codec1_21;
    }
    return codec1_20_5;
  }

  /** Update-tags NBT for protocols ≥766. */
  public CompoundBinaryTag tags(int protocol) {
    if (protocol >= 772) {
      return tags1_21_7;
    }
    if (protocol >= 771) {
      return tags1_21_6;
    }
    if (protocol >= 770) {
      return tags1_21_5;
    }
    if (protocol >= 769) {
      return tags1_21_4;
    }
    if (protocol >= 768) {
      return tags1_21_2;
    }
    if (protocol >= 767) {
      return tags1_21;
    }
    return tags1_20_5;
  }

  /** Play-era codec (JoinGame dimension NBT) per the vendored brackets. */
  public CompoundBinaryTag codecPlay(int protocol) {
    if (protocol >= 763) {
      return codec1_20;
    }
    if (protocol >= 759) {
      if (protocol >= 762) {
        return codec1_19_4;
      }
      if (protocol >= 760) {
        return codec1_19_1;
      }
      return codec1_19;
    }
    if (protocol >= 758) {
      return codec1_18_2;
    }
    if (protocol >= 755) {
      return codec1_17;
    }
    if (protocol >= 751) {
      return codec1_16_2;
    }
    return codec1_16;
  }

  /** Dimension-element NBT pair member for JoinGame on 757-762 era codecs. */
  public CompoundBinaryTag dimensionElement(int protocol, String dimensionName) {
    CompoundBinaryTag codec = codecPlay(protocol);
    ListBinaryTag dimensions = codec.getCompound("minecraft:dimension_type").getList("value");
    for (int i = 0; i < dimensions.size(); i++) {
      CompoundBinaryTag dimension = (CompoundBinaryTag) dimensions.get(i);
      if (dimension.getString("name").startsWith(dimensionName)) {
        return dimension.getCompound("element");
      }
    }
    return ((CompoundBinaryTag) dimensions.get(0)).getCompound("element");
  }

  /**
   * Varint dimension id for JoinGame ≥766: index of the first {@code minecraft:dimension_type}
   * entry whose name starts with the dimension name, from the id-source codec per the spec
   * (≥771→1_21_6, ≥770→1_21_5, ≥769→1_21_4, else 1_21_2).
   */
  public int dimensionId(int protocol, String dimensionName) {
    CompoundBinaryTag idSource;
    if (protocol >= 771) {
      idSource = codec1_21_6;
    } else if (protocol >= 770) {
      idSource = codec1_21_5;
    } else if (protocol >= 769) {
      idSource = codec1_21_4;
    } else {
      idSource = codec1_21_2;
    }
    return indexOfDimension(idSource, dimensionName);
  }

  /**
   * All registry entries of a codec's top-level registry type, in wire order: string type, count,
   * then name/hasElement/element tuples.
   */
  public List<Entry> registryEntries(CompoundBinaryTag codec, String registryType) {
    ListBinaryTag values = codec.getCompound(registryType).getList("value");
    List<Entry> entries = new ArrayList<>(values.size());
    for (BinaryTag tag : values) {
      CompoundBinaryTag entryTag = (CompoundBinaryTag) tag;
      entries.add(new Entry(entryTag.getString("name"), entryTag.getCompound("element", null)));
    }
    return entries;
  }

  /** Flattens a tags NBT compound into the wire shape written by PacketUpdateTags. */
  public void writeTags(ByteBuf buf, CompoundBinaryTag tags) {
    Set<String> registryTypes = tags.keySet();
    Buf.writeVarInt(buf, registryTypes.size());
    for (String registryType : registryTypes) {
      Buf.writeString(buf, registryType);
      CompoundBinaryTag sub = tags.getCompound(registryType);
      Set<String> tagNames = sub.keySet();
      Buf.writeVarInt(buf, tagNames.size());
      for (String tagName : tagNames) {
        Buf.writeString(buf, tagName);
        ListBinaryTag ids = (ListBinaryTag) sub.get(tagName);
        Buf.writeVarInt(buf, ids.size());
        for (BinaryTag id : ids) {
          Buf.writeVarInt(buf, ((IntBinaryTag) id).value());
        }
      }
    }
  }

  private int indexOfDimension(CompoundBinaryTag codec, String dimensionName) {
    ListBinaryTag dimensions = codec.getCompound("minecraft:dimension_type").getList("value");
    for (int i = 0; i < dimensions.size(); i++) {
      CompoundBinaryTag dimension = (CompoundBinaryTag) dimensions.get(i);
      if (dimension.getString("name").startsWith(dimensionName)) {
        return i;
      }
    }
    return 0;
  }

  private static CompoundBinaryTag read(String name) throws IOException {
    try (InputStream in =
        RegistryData.class.getClassLoader().getResourceAsStream("dimension/" + name)) {
      if (in == null) {
        throw new IOException("Missing dimension resource: " + name);
      }
      return BinaryTagIO.unlimitedReader().read(in, BinaryTagIO.Compression.GZIP);
    }
  }
}

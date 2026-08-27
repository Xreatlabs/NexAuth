/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;

/**
 * Minecraft wire primitives over a raw {@link ByteBuf}. Neither the limbo pipeline nor its payload
 * classes wrap the buffer, so every helper takes the buffer explicitly.
 */
final class Buf {

  private Buf() {}

  static int readVarInt(ByteBuf buf) {
    int readable = buf.readableBytes();
    if (readable == 0) {
      throw new DecoderException("Empty buffer");
    }

    int k = buf.readByte();
    if ((k & 0x80) != 128) {
      return k;
    }

    int maxRead = Math.min(5, readable);
    int i = k & 0x7F;
    for (int j = 1; j < maxRead; j++) {
      k = buf.readByte();
      i |= (k & 0x7F) << j * 7;
      if ((k & 0x80) != 128) {
        return i;
      }
    }
    throw new DecoderException("Bad VarInt");
  }

  static void writeVarInt(ByteBuf buf, int value) {
    if ((value & (0xFFFFFFFF << 7)) == 0) {
      buf.writeByte(value);
    } else if ((value & (0xFFFFFFFF << 14)) == 0) {
      int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
      buf.writeShort(w);
    } else if ((value & (0xFFFFFFFF << 21)) == 0) {
      int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
      buf.writeMedium(w);
    } else if ((value & (0xFFFFFFFF << 28)) == 0) {
      int w =
          (value & 0x7F | 0x80) << 24
              | (((value >>> 7) & 0x7F | 0x80) << 16)
              | ((value >>> 14) & 0x7F | 0x80) << 8
              | (value >>> 21);
      buf.writeInt(w);
    } else {
      int w =
          (value & 0x7F | 0x80) << 24
              | ((value >>> 7) & 0x7F | 0x80) << 16
              | ((value >>> 14) & 0x7F | 0x80) << 8
              | ((value >>> 21) & 0x7F | 0x80);
      buf.writeInt(w);
      buf.writeByte(value >>> 28);
    }
  }

  static String readString(ByteBuf buf, int maxLen) {
    int len = readVarInt(buf);
    if (len > maxLen * 3) {
      throw new DecoderException(
          "Cannot receive string longer than " + maxLen * 3 + " (got " + len + " bytes)");
    }

    String s = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
    buf.readerIndex(buf.readerIndex() + len);

    if (s.length() > maxLen) {
      throw new DecoderException(
          "Cannot receive string longer than " + maxLen + " (got " + s.length() + " characters)");
    }

    return s;
  }

  static void writeString(ByteBuf buf, CharSequence str) {
    writeVarInt(buf, ByteBufUtil.utf8Bytes(str));
    buf.writeCharSequence(str, StandardCharsets.UTF_8);
  }

  static UUID readUuid(ByteBuf buf) {
    long msb = buf.readLong();
    long lsb = buf.readLong();
    return new UUID(msb, lsb);
  }

  static void writeUuid(ByteBuf buf, UUID uuid) {
    buf.writeLong(uuid.getMostSignificantBits());
    buf.writeLong(uuid.getLeastSignificantBits());
  }

  /** Nameless NBT for 1.20.2+ (protocol 764+), named with an empty name below that. */
  static void writeCompoundTag(ByteBuf buf, CompoundBinaryTag tag, int protocol) {
    try (ByteBufOutputStream stream = new ByteBufOutputStream(buf)) {
      if (protocol >= 764) {
        BinaryTagIO.writer().writeNameless(tag, stream, BinaryTagIO.Compression.NONE);
      } else {
        BinaryTagIO.writer().writeNamed(Map.entry("", tag), stream, BinaryTagIO.Compression.NONE);
      }
    } catch (IOException e) {
      throw new EncoderException("Cannot write NBT CompoundTag", e);
    }
  }
}

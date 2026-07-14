/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.Test;

class NbtMessageUtilTest {

  @Test
  void createsMessageFromJsonObject() {
    var message = NbtMessageUtil.create("{\"text\":\"Hello\"}");

    assertEquals("{\"text\":\"Hello\"}", message.getJson());
    assertEquals("Hello", ((StringBinaryTag) message.getTag().get("text")).value());
  }

  @Test
  void convertsBooleanAndNumericArrayJson() {
    var booleanTag = NbtMessageUtil.fromJson(JsonParser.parseString("true"));
    var byteArrayTag = NbtMessageUtil.fromJson(JsonParser.parseString("[1,2,3]"));
    var intArrayTag = NbtMessageUtil.fromJson(JsonParser.parseString("[40000,50000,60000]"));
    var longArrayTag = NbtMessageUtil.fromJson(JsonParser.parseString("[2147483648,2147483649]"));

    assertEquals((byte) 1, ((ByteBinaryTag) booleanTag).value());
    assertInstanceOf(ByteArrayBinaryTag.class, byteArrayTag);
    assertEquals(3, ((ByteArrayBinaryTag) byteArrayTag).value().length);
    assertInstanceOf(IntArrayBinaryTag.class, intArrayTag);
    assertEquals(3, ((IntArrayBinaryTag) intArrayTag).value().length);
    assertInstanceOf(LongArrayBinaryTag.class, longArrayTag);
    assertEquals(2, ((LongArrayBinaryTag) longArrayTag).value().length);
  }

  @Test
  void convertsNumericBoundariesAndDecimals() {
    assertInstanceOf(ByteBinaryTag.class, NbtMessageUtil.fromJson(JsonParser.parseString("127")));
    assertInstanceOf(ShortBinaryTag.class, NbtMessageUtil.fromJson(JsonParser.parseString("128")));
    assertInstanceOf(IntBinaryTag.class, NbtMessageUtil.fromJson(JsonParser.parseString("40000")));
    assertInstanceOf(
        LongBinaryTag.class, NbtMessageUtil.fromJson(JsonParser.parseString("2147483648")));
    assertInstanceOf(DoubleBinaryTag.class, NbtMessageUtil.fromJson(JsonParser.parseString("1.5")));
    assertInstanceOf(DoubleBinaryTag.class, NbtMessageUtil.fromJson(JsonParser.parseString("1e3")));
  }

  @Test
  void convertsJsonNullToEndTag() {
    assertInstanceOf(EndBinaryTag.class, NbtMessageUtil.fromJson(JsonNull.INSTANCE));
  }
}

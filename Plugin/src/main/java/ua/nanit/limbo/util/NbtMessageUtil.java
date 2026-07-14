/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.nbt.*;
import ua.nanit.limbo.protocol.NbtMessage;

public class NbtMessageUtil {

  public static NbtMessage create(String json) {
    CompoundBinaryTag compoundBinaryTag =
        (CompoundBinaryTag) fromJson(JsonParser.parseString(json));

    return new NbtMessage(json, compoundBinaryTag);
  }

  public static BinaryTag fromJson(JsonElement json) {
    if (json instanceof JsonPrimitive) {
      JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
      if (jsonPrimitive.isNumber()) {
        return fromNumber(jsonPrimitive);
      } else if (jsonPrimitive.isString()) {
        return StringBinaryTag.stringBinaryTag(jsonPrimitive.getAsString());
      } else if (jsonPrimitive.isBoolean()) {
        return ByteBinaryTag.byteBinaryTag(jsonPrimitive.getAsBoolean() ? (byte) 1 : (byte) 0);
      } else {
        throw new IllegalArgumentException("Unknown JSON primitive: " + jsonPrimitive);
      }
    } else if (json instanceof JsonObject) {
      CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
      for (Map.Entry<String, JsonElement> property : ((JsonObject) json).entrySet()) {
        builder.put(property.getKey(), fromJson(property.getValue()));
      }

      return builder.build();
    } else if (json instanceof JsonArray) {
      List<JsonElement> jsonArray = ((JsonArray) json).asList();

      if (jsonArray.isEmpty()) {
        return ListBinaryTag.listBinaryTag(
            EndBinaryTag.endBinaryTag().type(), Collections.emptyList());
      }

      BinaryTagType<ByteBinaryTag> tagByteType = ByteBinaryTag.ZERO.type();
      BinaryTagType<IntBinaryTag> tagIntType = IntBinaryTag.intBinaryTag(0).type();
      BinaryTagType<LongBinaryTag> tagLongType = LongBinaryTag.longBinaryTag(0).type();

      BinaryTag listTag;
      BinaryTagType<? extends BinaryTag> listType = fromJson(jsonArray.get(0)).type();
      if (listType.equals(tagByteType)) {
        byte[] bytes = new byte[jsonArray.size()];
        for (int i = 0; i < bytes.length; i++) {
          bytes[i] = jsonArray.get(i).getAsNumber().byteValue();
        }

        listTag = ByteArrayBinaryTag.byteArrayBinaryTag(bytes);
      } else if (listType.equals(tagIntType)) {
        int[] ints = new int[jsonArray.size()];
        for (int i = 0; i < ints.length; i++) {
          ints[i] = jsonArray.get(i).getAsNumber().intValue();
        }

        listTag = IntArrayBinaryTag.intArrayBinaryTag(ints);
      } else if (listType.equals(tagLongType)) {
        long[] longs = new long[jsonArray.size()];
        for (int i = 0; i < longs.length; i++) {
          longs[i] = jsonArray.get(i).getAsNumber().longValue();
        }

        listTag = LongArrayBinaryTag.longArrayBinaryTag(longs);
      } else {
        List<BinaryTag> tagItems = new ArrayList<>(jsonArray.size());

        for (JsonElement jsonEl : jsonArray) {
          BinaryTag subTag = fromJson(jsonEl);
          if (subTag.type() != listType) {
            throw new IllegalArgumentException("Cannot convert mixed JsonArray to Tag");
          }

          tagItems.add(subTag);
        }

        listTag = ListBinaryTag.listBinaryTag(listType, tagItems);
      }

      return listTag;
    } else if (json instanceof JsonNull) {
      return EndBinaryTag.endBinaryTag();
    }

    throw new IllegalArgumentException("Unknown JSON element: " + json);
  }

  private static BinaryTag fromNumber(JsonPrimitive jsonPrimitive) {
    String value = jsonPrimitive.getAsString();

    if (value.contains(".") || value.contains("e") || value.contains("E")) {
      return DoubleBinaryTag.doubleBinaryTag(jsonPrimitive.getAsDouble());
    }

    long longValue = jsonPrimitive.getAsLong();
    if (longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE) {
      return ByteBinaryTag.byteBinaryTag((byte) longValue);
    }
    if (longValue >= Short.MIN_VALUE && longValue <= Short.MAX_VALUE) {
      return ShortBinaryTag.shortBinaryTag((short) longValue);
    }
    if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
      return IntBinaryTag.intBinaryTag((int) longValue);
    }
    return LongBinaryTag.longBinaryTag(longValue);
  }
}

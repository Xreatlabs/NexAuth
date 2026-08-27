/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Player-info forwarding verification for the embedded limbo: MODERN (Velocity modern forwarding),
 * LEGACY (BungeeCave-style host split), BUNGEEGUARD and NONE (offline UUID derivation).
 */
public final class Forwarding {

  public enum Mode {
    NONE,
    LEGACY,
    MODERN,
    BUNGEEGUARD
  }

  /** Result of a verified forwarding payload; address, uuid and username in wire order. */
  public record Profile(String address, UUID uuid, String username) {}

  private Forwarding() {}

  /**
   * Verifies a modern-forwarding {@code velocity:player_info} payload and reads the profile. The
   * buffer arrives mid-packet (packet id, message id and success boolean already consumed), so the
   * 32-byte signature starts at the current reader index and the signed body follows it.
   */
  public static Profile verifyModern(ByteBuf data, byte[] secret) {
    if (data.readableBytes() < 32) {
      return null;
    }

    int bodyIndex = data.readerIndex() + 32;
    byte[] signature = new byte[32];
    data.readBytes(signature);
    byte[] remaining = new byte[data.readableBytes()];
    data.getBytes(bodyIndex, remaining);

    byte[] mySignature;
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      mySignature = mac.doFinal(remaining);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }

    if (!MessageDigest.isEqual(signature, mySignature)) {
      return null;
    }

    data.readerIndex(bodyIndex);
    if (Buf.readVarInt(data) != 1) {
      return null;
    }

    String address = Buf.readString(data, 32767);
    UUID uuid = Buf.readUuid(data);
    String username = Buf.readString(data, 32767);
    return new Profile(address, uuid, username);
  }

  /**
   * Splits a LEGACY forwarding host. Returns remote host + uuid on 3-or-4 part splits, null
   * otherwise. The caller keeps the original host when null is returned.
   */
  public record LegacyHost(String host, UUID uuid) {}

  public static LegacyHost splitLegacy(String handshakeHost) {
    String[] split = handshakeHost.split("\00");
    if (split.length != 3 && split.length != 4) {
      return null;
    }
    return new LegacyHost(split[1], uuidFromString(split[2]));
  }

  /**
   * Verifies a BUNGEEGUARD handshake host: {@code host\0address\0uuid\0json-properties}. Returns
   * the profile when the {@code bungeeguard-token} property matches, null otherwise.
   */
  public static Profile verifyBungeeGuard(String handshakeHost, List<String> tokens) {
    String[] split = handshakeHost.split("\00");
    if (split.length != 4) {
      return null;
    }

    String address = split[1];
    UUID uuid = uuidFromString(split[2]);

    JsonArray properties;
    try {
      properties = JsonParser.array().from(split[3]);
    } catch (JsonParserException e) {
      return null;
    }

    String token = null;
    for (Object obj : properties) {
      if (obj instanceof JsonObject prop && "bungeeguard-token".equals(prop.getString("name"))) {
        token = prop.getString("value");
        break;
      }
    }

    if (token == null || !tokens.contains(token)) {
      return null;
    }

    return new Profile(address, uuid, "");
  }

  /** Offline-mode UUID as derived by a vanilla/Bukkit {@code OfflinePlayer}. */
  public static UUID offlineUuid(String username) {
    return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
  }

  /** Parses a dashed or 32-hex-digit UUID the way BungeeCord forwards them. */
  public static UUID uuidFromString(String str) {
    if (str.contains("-")) {
      return UUID.fromString(str);
    }
    return UUID.fromString(
        str.replaceFirst(
            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
            "$1-$2-$3-$4-$5"));
  }
}

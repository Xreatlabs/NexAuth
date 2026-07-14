/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import ua.nanit.limbo.protocol.registry.Version;

/**
 * PacketSnapshot encodes a packet to byte array for each MC version. Some versions have the same
 * snapshot, so there are mappings to avoid data copying
 */
public class PacketSnapshot implements PacketOut {

  private final Class<? extends PacketOut> packetClazz;
  private final Map<Version, byte[]> versionMessages = new EnumMap<>(Version.class);
  private final Map<Version, Version> mappings = new EnumMap<>(Version.class);

  public PacketSnapshot(Class<? extends PacketOut> packetClazz) {
    this.packetClazz = packetClazz;
  }

  public Class<? extends PacketOut> getPacketClass() {
    return packetClazz;
  }

  public void encode(Function<Version, PacketOut> packetComputeFunction) {
    Map<Integer, Version> hashes = new HashMap<>();

    for (Version version : Version.values()) {
      if (version.equals(Version.UNDEFINED)) continue;

      ByteMessage encodedMessage = ByteMessage.create();
      packetComputeFunction.apply(version).encode(encodedMessage, version);

      int hash = encodedMessage.hashCode();
      Version hashed = hashes.get(hash);

      if (hashed != null) {
        mappings.put(version, hashed);
      } else {
        hashes.put(hash, version);
        mappings.put(version, version);
        versionMessages.put(version, encodedMessage.toByteArray());
      }

      encodedMessage.release();
    }
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    Version mapped = mappings.get(version);
    byte[] message = versionMessages.get(mapped);

    if (message != null) msg.writeBytes(message);
    else throw new IllegalArgumentException("No mappings for version " + version);
  }

  @Override
  public String toString() {
    return packetClazz.getSimpleName();
  }

  public static PacketSnapshot of(PacketOut packet) {
    return of(packet.getClass(), version -> packet);
  }

  public static PacketSnapshot of(
      Class<? extends PacketOut> packetClazz, Function<Version, PacketOut> packetComputeFunction) {
    PacketSnapshot snapshot = new PacketSnapshot(packetClazz);
    snapshot.encode(packetComputeFunction);
    return snapshot;
  }
}

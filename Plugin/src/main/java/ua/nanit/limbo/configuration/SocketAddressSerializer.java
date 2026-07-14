/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.configuration;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class SocketAddressSerializer implements TypeSerializer<SocketAddress> {

  @Override
  public SocketAddress deserialize(Type type, ConfigurationNode node) {
    String ip = node.node("ip").getString();
    int port = node.node("port").getInt();
    SocketAddress address;

    if (ip == null || ip.isEmpty()) {
      address = new InetSocketAddress(port);
    } else {
      address = new InetSocketAddress(ip, port);
    }

    return address;
  }

  @Override
  public void serialize(Type type, @Nullable SocketAddress obj, ConfigurationNode node) {}
}

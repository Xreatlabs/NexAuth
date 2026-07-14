/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.data;

import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ua.nanit.limbo.util.Colors;

public class PingData {

  private String version;
  private String description;
  private int protocol;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getProtocol() {
    return protocol;
  }

  public void setProtocol(int protocol) {
    this.protocol = protocol;
  }

  public static class Serializer implements TypeSerializer<PingData> {

    @Override
    public PingData deserialize(Type type, ConfigurationNode node) {
      PingData pingData = new PingData();
      pingData.setDescription(Colors.of(node.node("description").getString("")));
      pingData.setVersion(Colors.of(node.node("version").getString("")));
      pingData.setProtocol(node.node("protocol").getInt(-1));
      return pingData;
    }

    @Override
    public void serialize(Type type, @Nullable PingData obj, ConfigurationNode node) {}
  }
}

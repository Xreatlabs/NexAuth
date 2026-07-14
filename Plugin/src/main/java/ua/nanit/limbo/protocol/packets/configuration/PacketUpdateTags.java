/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol.packets.configuration;

import java.util.List;
import java.util.Map;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketUpdateTags implements PacketOut {

  private Map<String, Map<String, List<Integer>>> tags;

  public Map<String, Map<String, List<Integer>>> getTags() {
    return tags;
  }

  public void setTags(Map<String, Map<String, List<Integer>>> tags) {
    this.tags = tags;
  }

  @Override
  public void encode(ByteMessage msg, Version version) {
    msg.writeVarInt(this.tags.size());
    for (Map.Entry<String, Map<String, List<Integer>>> entry : this.tags.entrySet()) {
      msg.writeString(entry.getKey());

      Map<String, List<Integer>> subTags = entry.getValue();
      msg.writeVarInt(subTags.size());
      for (Map.Entry<String, List<Integer>> subEntry : subTags.entrySet()) {
        msg.writeString(subEntry.getKey());

        List<Integer> ids = subEntry.getValue();
        msg.writeVarInt(ids.size());
        for (int id : ids) {
          msg.writeVarInt(id);
        }
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

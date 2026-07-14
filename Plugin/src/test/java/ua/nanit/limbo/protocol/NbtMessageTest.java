/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.junit.jupiter.api.Test;

class NbtMessageTest {

  @Test
  void storesJsonAndTag() {
    var tag = CompoundBinaryTag.empty();
    var message = new NbtMessage("{\"text\":\"Hello\"}", tag);

    assertEquals("{\"text\":\"Hello\"}", message.getJson());
    assertSame(tag, message.getTag());

    var newTag = CompoundBinaryTag.builder().putString("key", "value").build();
    message.setJson("updated");
    message.setTag(newTag);

    assertEquals("updated", message.getJson());
    assertSame(newTag, message.getTag());
  }
}

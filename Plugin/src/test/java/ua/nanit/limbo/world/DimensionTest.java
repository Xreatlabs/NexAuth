/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.junit.jupiter.api.Test;

class DimensionTest {

  @Test
  void storesDimensionMetadata() {
    var data = CompoundBinaryTag.builder().putString("effects", "minecraft:overworld").build();
    var dimension = new Dimension(0, "minecraft:overworld", data);

    assertEquals(0, dimension.getId());
    assertEquals("minecraft:overworld", dimension.getName());
    assertSame(data, dimension.getData());
  }
}

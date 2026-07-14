/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;

public class Dimension {

  private final int id;
  private final String name;
  private final CompoundBinaryTag data;

  public Dimension(int id, String name, CompoundBinaryTag data) {
    this.id = id;
    this.name = name;
    this.data = data;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public CompoundBinaryTag getData() {
    return data;
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.server.data;

import java.lang.reflect.Type;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ua.nanit.limbo.protocol.NbtMessage;
import ua.nanit.limbo.util.Colors;
import ua.nanit.limbo.util.NbtMessageUtil;

public class BossBar {

  private NbtMessage text;
  private float health;
  private Color color;
  private Division division;

  public NbtMessage getText() {
    return text;
  }

  public float getHealth() {
    return health;
  }

  public Color getColor() {
    return color;
  }

  public Division getDivision() {
    return division;
  }

  public void setText(NbtMessage text) {
    this.text = text;
  }

  public void setHealth(float health) {
    this.health = health;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public void setDivision(Division division) {
    this.division = division;
  }

  public enum Color {
    PINK(0),
    BLUE(1),
    RED(2),
    GREEN(3),
    YELLOW(4),
    PURPLE(5),
    WHITE(6);

    private final int index;

    Color(int index) {
      this.index = index;
    }

    public int getIndex() {
      return index;
    }
  }

  public enum Division {
    SOLID(0),
    DASHES_6(1),
    DASHES_10(2),
    DASHES_12(3),
    DASHES_20(4);

    private final int index;

    Division(int index) {
      this.index = index;
    }

    public int getIndex() {
      return index;
    }
  }

  public static class Serializer implements TypeSerializer<BossBar> {

    @Override
    public BossBar deserialize(Type type, ConfigurationNode node) throws SerializationException {
      BossBar bossBar = new BossBar();

      bossBar.setText(NbtMessageUtil.create(Colors.of(node.node("text").getString(""))));
      bossBar.setHealth(node.node("health").getFloat());

      if (bossBar.getHealth() < 0 || bossBar.getHealth() > 1)
        throw new SerializationException("BossBar health value must be between 0.0 and 1.0");

      try {
        bossBar.setColor(Color.valueOf(node.node("color").getString("").toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException e) {
        throw new SerializationException("Invalid bossbar color");
      }

      try {
        bossBar.setDivision(
            Division.valueOf(node.node("division").getString("").toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException e) {
        throw new SerializationException("Invalid bossbar division");
      }

      return bossBar;
    }

    @Override
    public void serialize(Type type, @Nullable BossBar obj, ConfigurationNode node) {}
  }
}

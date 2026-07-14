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
import ua.nanit.limbo.protocol.NbtMessage;
import ua.nanit.limbo.util.Colors;
import ua.nanit.limbo.util.NbtMessageUtil;

public class Title {

  private NbtMessage title;
  private NbtMessage subtitle;
  private int fadeIn;
  private int stay;
  private int fadeOut;

  public NbtMessage getTitle() {
    return title;
  }

  public NbtMessage getSubtitle() {
    return subtitle;
  }

  public int getFadeIn() {
    return fadeIn;
  }

  public int getStay() {
    return stay;
  }

  public int getFadeOut() {
    return fadeOut;
  }

  public void setTitle(NbtMessage title) {
    this.title = title;
  }

  public void setSubtitle(NbtMessage subtitle) {
    this.subtitle = subtitle;
  }

  public void setFadeIn(int fadeIn) {
    this.fadeIn = fadeIn;
  }

  public void setStay(int stay) {
    this.stay = stay;
  }

  public void setFadeOut(int fadeOut) {
    this.fadeOut = fadeOut;
  }

  public static class Serializer implements TypeSerializer<Title> {

    @Override
    public Title deserialize(Type type, ConfigurationNode node) {
      Title title = new Title();
      title.setTitle(NbtMessageUtil.create(Colors.of(node.node("title").getString(""))));
      title.setSubtitle(NbtMessageUtil.create(Colors.of(node.node("subtitle").getString(""))));
      title.setFadeIn(node.node("fadeIn").getInt(10));
      title.setStay(node.node("stay").getInt(100));
      title.setFadeOut(node.node("fadeOut").getInt(10));
      return title;
    }

    @Override
    public void serialize(Type type, @Nullable Title obj, ConfigurationNode node) {
      // Not used
    }
  }
}

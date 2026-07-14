/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.common.command.InvalidCommandArgument;
import xyz.xreatlabs.nexauth.common.config.key.ConfigurationKey;

class GeneralUtilTest {

  @Test
  void readsInputAndFindsFurthestCause() throws Exception {
    var input = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
    var root = new IllegalStateException("root");
    var wrapped = new RuntimeException(new RuntimeException(root));

    assertEquals("hello", GeneralUtil.readInput(input));
    assertSame(root, GeneralUtil.getFurthestCause(wrapped));
  }

  @Test
  void convertsUuidFormatsAndOfflineNames() {
    var uuid = UUID.randomUUID();
    var raw = uuid.toString().replace("-", "");

    assertEquals(uuid, GeneralUtil.fromUnDashedUUID(raw));
    assertEquals(
        UUID.nameUUIDFromBytes(("OfflinePlayer:" + "Alex").getBytes(StandardCharsets.UTF_8)),
        GeneralUtil.getCrackedUUIDFromName("Alex"));
    assertNull(GeneralUtil.getCrackedUUIDFromName(null));
  }

  @Test
  void formatsComponentsAndFindsFields() {
    var component = Component.text("Hello ").append(Component.text("%name%"));
    var formatted =
        GeneralUtil.formatComponent(
            (net.kyori.adventure.text.TextComponent) component, Map.of("%name%", "Steve"));

    assertEquals("Hello Steve", PlainTextComponentSerializer.plainText().serialize(formatted));
    assertEquals(
        String.class, GeneralUtil.getFieldByType(FieldHolder.class, String.class).getType());
  }

  @Test
  void extractsConfigurationKeysAndGeneratesAlphanumericText() {
    var keys = GeneralUtil.extractKeys(KeyHolder.class);
    var random = GeneralUtil.generateAlphanumericText(32);

    assertEquals(1, keys.size());
    assertEquals(TEST_KEY, keys.getFirst());
    assertEquals(32, random.length());
    assertTrue(random.chars().allMatch(ch -> Character.isLetterOrDigit(ch)));
  }

  @Test
  void runAsyncCompletesNormallyAndExceptionally() throws Exception {
    GeneralUtil.runAsync(() -> {}).toCompletableFuture().get();

    var ex =
        assertThrows(
            ExecutionException.class,
            () ->
                GeneralUtil.runAsync(
                        () -> {
                          throw new InvalidCommandArgument(Component.text("bad"));
                        })
                    .toCompletableFuture()
                    .get());
    assertInstanceOf(InvalidCommandArgument.class, ex.getCause());
  }

  public static final class FieldHolder {
    public String name;
    public int value;
  }

  public static final ConfigurationKey<String> TEST_KEY =
      new ConfigurationKey<>("test.key", "value", "comment", (helper, path) -> "computed");

  public static final class KeyHolder {
    public static final ConfigurationKey<String> KEY = TEST_KEY;
    public static final String OTHER = "ignore";
  }
}

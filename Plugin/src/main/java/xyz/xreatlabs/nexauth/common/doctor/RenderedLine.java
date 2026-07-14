/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.doctor;

import java.util.List;
import java.util.Objects;

public record RenderedLine(String messageKey, List<String> replacements) {

  public RenderedLine {
    Objects.requireNonNull(messageKey, "messageKey");
    Objects.requireNonNull(replacements, "replacements");
  }

  public RenderedLine(String messageKey, String... replacements) {
    this(messageKey, List.of(replacements));
  }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.protocol;

import ua.nanit.limbo.protocol.registry.Version;

@FunctionalInterface
public interface MetadataWriter {

  void writeData(ByteMessage message, Version version);
}

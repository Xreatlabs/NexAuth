/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

public record LimboSettings(
    int maxPlayers,
    String motd,
    int keepAliveIntervalSeconds,
    int readTimeoutSeconds,
    Forwarding.Mode forwardingMode,
    byte[] forwardingSecret) {}

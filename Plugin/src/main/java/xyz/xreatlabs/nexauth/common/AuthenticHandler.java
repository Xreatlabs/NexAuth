/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common;

import xyz.xreatlabs.nexauth.api.PlatformHandle;

public class AuthenticHandler<P, S> {

    protected final AuthenticNexAuth<P, S> plugin;
    protected final PlatformHandle<P, S> platformHandle;

    public AuthenticHandler(AuthenticNexAuth<P, S> plugin) {
        this.plugin = plugin;
        this.platformHandle = plugin.getPlatformHandle();
    }
}

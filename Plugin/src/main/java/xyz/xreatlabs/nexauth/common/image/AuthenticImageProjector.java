/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.image;

import xyz.xreatlabs.nexauth.api.image.ImageProjector;
import xyz.xreatlabs.nexauth.common.AuthenticHandler;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;

public abstract class AuthenticImageProjector<P, S> extends AuthenticHandler<P, S> implements ImageProjector<P> {

    public AuthenticImageProjector(AuthenticNexAuth<P, S> plugin) {
        super(plugin);
    }

    public abstract void enable();

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.provider;

import xyz.xreatlabs.nexauth.api.NexAuthPlugin;

/**
 * This class is used to obtain the instance of the plugin
 *
 * @param <P> The type of the player
 * @param <S> The type of the server
 */
public interface NexAuthProvider<P, S> {

    /**
     * Gets the instance of the plugin
     *
     * @return the instance of the plugin
     */
    NexAuthPlugin<P, S> getNexAuth();

}

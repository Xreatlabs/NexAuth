/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database.connector;

import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.config.key.ConfigurationKey;

public abstract class AuthenticDatabaseConnector<E extends Exception, I> implements DatabaseConnector<E, I> {

    protected final AuthenticNexAuth<?, ?> plugin;
    private final String prefix;
    protected boolean connected = true;

    public AuthenticDatabaseConnector(AuthenticNexAuth<?, ?> plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
    }

    @Override
    public boolean connected() {
        return connected;
    }

    public <T> T get(ConfigurationKey<T> key) {
        var value = key.getter().apply(plugin.getConfiguration().getHelper(), prefix + key.key());
        return value == null ? key.defaultValue() : value;
    }

}

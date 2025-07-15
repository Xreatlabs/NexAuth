/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database;

import xyz.xreatlabs.nexauth.api.database.ReadWriteDatabaseProvider;
import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;

public abstract class AuthenticDatabaseProvider<C extends DatabaseConnector<?, ?>> implements ReadWriteDatabaseProvider {

    protected final C connector;
    protected final AuthenticNexAuth<?, ?> plugin;

    protected AuthenticDatabaseProvider(C connector, AuthenticNexAuth<?, ?> plugin) {
        this.connector = connector;
        this.plugin = plugin;
    }

    public void validateSchema() {
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.migrate;

import xyz.xreatlabs.nexauth.api.database.ReadDatabaseProvider;
import xyz.xreatlabs.nexauth.api.database.User;

import java.util.Collection;
import java.util.UUID;

public abstract class MigrateReadProvider implements ReadDatabaseProvider {

    @Override
    public User getByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> getByIP(String ip) {
        throw new UnsupportedOperationException();
    }

}

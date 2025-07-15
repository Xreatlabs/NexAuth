/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.migrate.messages;

import xyz.xreatlabs.nexauth.api.Logger;
import xyz.xreatlabs.nexauth.common.config.ConfigurateHelper;
import xyz.xreatlabs.nexauth.common.config.MessageKeys;
import xyz.xreatlabs.nexauth.common.config.migrate.ConfigurationMigrator;

public class ThirdMessagesMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        rename("kick-no-server", MessageKeys.KICK_NO_LOBBY, helper);
    }
}

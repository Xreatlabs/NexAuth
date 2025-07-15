/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config.migrate.config;

import xyz.xreatlabs.nexauth.api.Logger;
import xyz.xreatlabs.nexauth.common.config.ConfigurateHelper;
import xyz.xreatlabs.nexauth.common.config.migrate.ConfigurationMigrator;

public class SeventhConfigurationMigrator implements ConfigurationMigrator {

    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var kickOnWrongPassword = helper.getBoolean("kick-on-wrong-password");

        helper.set("kick-on-wrong-password", null);
        if (kickOnWrongPassword)
            helper.set("max-login-attempts", 1);
        else
            helper.set("max-login-attempts", -1);
    }

}

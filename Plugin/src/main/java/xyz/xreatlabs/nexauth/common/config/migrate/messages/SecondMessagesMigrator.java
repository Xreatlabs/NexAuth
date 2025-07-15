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

public class SecondMessagesMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        logger.warn("Sorry, but I've needed to reset the totp-show-info message because the process has significantly changed. Here is the original: " + helper.getString("totp-show-info"));

        helper.set("totp-show-info", MessageKeys.TOTP_SHOW_INFO.defaultValue());
    }
}

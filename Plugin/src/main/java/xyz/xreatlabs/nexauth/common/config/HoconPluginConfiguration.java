/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.config;

import xyz.xreatlabs.nexauth.api.BiHolder;
import xyz.xreatlabs.nexauth.api.NexAuthPlugin;
import xyz.xreatlabs.nexauth.api.Logger;
import xyz.xreatlabs.nexauth.api.configuration.CorruptedConfigurationException;
import xyz.xreatlabs.nexauth.common.config.key.ConfigurationKey;
import xyz.xreatlabs.nexauth.common.config.migrate.config.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static xyz.xreatlabs.nexauth.common.config.ConfigurationKeys.DEFAULT_CRYPTO_PROVIDER;
import static xyz.xreatlabs.nexauth.common.config.ConfigurationKeys.NEW_UUID_CREATOR;

public class HoconPluginConfiguration {

    private final Logger logger;
    private final Collection<BiHolder<Class<?>, String>> defaultKeys;
    private ConfigurateHelper helper;

    public HoconPluginConfiguration(Logger logger, Collection<BiHolder<Class<?>, String>> defaultKeys) {
        this.logger = logger;
        this.defaultKeys = new ArrayList<>(defaultKeys); //Make this independent on the original collection in case it gets modified
        this.defaultKeys.add(new BiHolder<>(ConfigurationKeys.class, "")); //Make sure the default configuration keys always have top priority
    }

    public ConfigurateHelper getHelper() {
        return helper;
    }

    public boolean reload(NexAuthPlugin<?, ?> plugin) throws IOException, CorruptedConfigurationException {
        var adept = new ConfigurateConfiguration(
                plugin.getDataFolder(),
                "config.conf",
                defaultKeys,
                """
                          !!THIS FILE IS WRITTEN IN THE HOCON FORMAT!!
                          The hocon format is very similar to JSON, but it has some extra features.
                          You can find more information about the format on the sponge wiki:
                          https://docs.spongepowered.org/stable/en/server/getting-started/configuration/hocon.html
                          ----------------------------------------------------------------------------------------
                          NexAuth Configuration
                          ----------------------------------------------------------------------------------------
                          This is the configuration file for NexAuth.
                          You can find more information about NexAuth on the github page:
                          https://github.com/Xreatlabs/NexAuth
                        """,
                logger,
                new FirstConfigurationMigrator(),
                new SecondConfigurationMigrator(),
                new ThirdConfigurationMigrator(),
                new FourthConfigurationMigrator(),
                new FifthConfigurationMigrator(),
                new SixthConfigurationMigrator(),
                new SeventhConfigurationMigrator(),
                new EightConfigurationMigrator()
        );

        var helperAdept = adept.getHelper();

        if (!adept.isNewlyCreated() && plugin.getCryptoProvider(helperAdept.get(DEFAULT_CRYPTO_PROVIDER)) == null) {
            throw new CorruptedConfigurationException("Crypto provider not found");
        }

        helper = helperAdept;

        return adept.isNewlyCreated();
    }

    public NewUUIDCreator getNewUUIDCreator() {
        var name = get(NEW_UUID_CREATOR);

        try {
            return NewUUIDCreator.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NewUUIDCreator.RANDOM;
        }
    }

    public <T> T get(ConfigurationKey<T> key) {
        return helper.get(key);
    }
}

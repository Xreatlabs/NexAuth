/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.xreatlabs.nexauth.api.database.connector.PostgreSQLDatabaseConnector;
import xyz.xreatlabs.nexauth.api.util.ThrowableFunction;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.config.ConfigurateHelper;
import xyz.xreatlabs.nexauth.common.config.key.ConfigurationKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

public class AuthenticPostgreSQLDatabaseConnector extends AuthenticDatabaseConnector<SQLException, Connection> implements PostgreSQLDatabaseConnector {

    private final HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    public AuthenticPostgreSQLDatabaseConnector(AuthenticNexAuth<?, ?> plugin, String prefix) {
        super(plugin, prefix);

        this.hikariConfig = new HikariConfig();

        hikariConfig.setPoolName("NexAuth PostgreSQL Pool");
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("ssl", "false");
        hikariConfig.addDataSourceProperty("sslmode", "disable");

        hikariConfig.setUsername(get(Configuration.USER));
        hikariConfig.setPassword(get(Configuration.PASSWORD));
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + get(Configuration.HOST) + ":" + get(Configuration.PORT) + "/" + get(Configuration.NAME) + "?sslmode=disable&autoReconnect=true&zeroDateTimeBehavior=convertToNull&ssl=false");
        hikariConfig.setMaxLifetime(get(Configuration.MAX_LIFE_TIME));
    }

    @Override
    public void connect() throws SQLException {
        dataSource = new HikariDataSource(hikariConfig);
        obtainInterface().close(); //Verify connection
        connected = true;
    }

    @Override
    public void disconnect() throws SQLException {
        connected = false;
        dataSource.close();
    }

    @Override
    public Connection obtainInterface() throws SQLException, IllegalStateException {
        if (!connected()) throw new IllegalStateException("Not connected to the database!");
        return dataSource.getConnection();
    }

    @Override
    public <V> V runQuery(ThrowableFunction<Connection, V, SQLException> function) throws IllegalStateException {
        try {
            try (var connection = obtainInterface()) {
                return function.apply(connection);
            }
        } catch (SQLTransientConnectionException e) {
            plugin.getLogger().error("!! LOST CONNECTION TO THE DATABASE, THE PROXY IS GOING TO SHUT DOWN TO PREVENT DAMAGE !!");
            e.printStackTrace();
            System.exit(1);
            //Won't return anyway
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Configuration {

        public static final ConfigurationKey<String> HOST = new ConfigurationKey<>(
                "host",
                "localhost",
                "The host of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<String> NAME = new ConfigurationKey<>(
                "database",
                "nexauth",
                "The name of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<String> PASSWORD = new ConfigurationKey<>(
                "password",
                "",
                "The password of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<Integer> PORT = new ConfigurationKey<>(
                "port",
                5432,
                "The port of the database.",
                ConfigurateHelper::getInt
        );

        public static final ConfigurationKey<String> USER = new ConfigurationKey<>(
                "user",
                "root",
                "The user of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<Integer> MAX_LIFE_TIME = new ConfigurationKey<>(
                "max-life-time",
                600000,
                "The maximum lifetime of a database connection in milliseconds. Don't touch this if you don't know what you're doing.",
                ConfigurateHelper::getInt
        );
    }
}

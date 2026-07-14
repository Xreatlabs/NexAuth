/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import xyz.xreatlabs.nexauth.api.database.connector.SQLiteDatabaseConnector;
import xyz.xreatlabs.nexauth.api.util.ThrowableFunction;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.config.ConfigurateHelper;
import xyz.xreatlabs.nexauth.common.config.ConfigurationKeys;
import xyz.xreatlabs.nexauth.common.config.key.ConfigurationKey;

public class AuthenticSQLiteDatabaseConnector
    extends AuthenticDatabaseConnector<SQLException, Connection>
    implements SQLiteDatabaseConnector {

  private final HikariConfig hikariConfig;
  private HikariDataSource dataSource;

  public AuthenticSQLiteDatabaseConnector(AuthenticNexAuth<?, ?> plugin, String prefix) {
    super(plugin, prefix);

    this.hikariConfig = new HikariConfig();

    hikariConfig.setPoolName("NexAuth SQLite Pool");
    hikariConfig.setDriverClassName("org.sqlite.JDBC");
    hikariConfig.setMaxLifetime(60000);
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    hikariConfig.setJdbcUrl(
        "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + get(Configuration.PATH));
  }

  @Override
  public void connect() throws SQLException {
    dataSource = new HikariDataSource(hikariConfig);
    obtainInterface().close(); // Verify connection
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
  public <V> V runQuery(ThrowableFunction<Connection, V, SQLException> function)
      throws IllegalStateException {
    try {
      try (var connection = obtainInterface()) {
        return function.apply(connection);
      }
    } catch (SQLTransientConnectionException e) {
      var retries =
          Math.max(0, plugin.getConfiguration().get(ConfigurationKeys.DATABASE_TRANSIENT_RETRIES));
      var delay =
          Math.max(
              0,
              plugin.getConfiguration().get(ConfigurationKeys.DATABASE_TRANSIENT_RETRY_DELAY_MS));

      for (int attempt = 0; attempt < retries; attempt++) {
        try {
          if (delay > 0) {
            Thread.sleep(delay);
          }

          try (var connection = obtainInterface()) {
            return function.apply(connection);
          }
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
          break;
        } catch (SQLTransientConnectionException ignored) {
        } catch (SQLException sqlException) {
          throw new RuntimeException(sqlException);
        }
      }

      plugin.handleFailurePolicy(
          "!! LOST CONNECTION TO THE DATABASE, FAILURE POLICY TRIGGERED !!",
          e,
          1,
          () ->
              plugin
                  .getLogger()
                  .error(
                      "Database operations are unavailable due to transient connection failures"));
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static final class Configuration {
    public static final ConfigurationKey<String> PATH =
        new ConfigurationKey<>(
            "path",
            "user-data.db",
            "Path to SQLite database file. Relative to plugin datafolder.",
            ConfigurateHelper::getString);
  }
}

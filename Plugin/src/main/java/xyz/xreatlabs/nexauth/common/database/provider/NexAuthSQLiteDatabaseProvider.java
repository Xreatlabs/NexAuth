/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database.provider;

import xyz.xreatlabs.nexauth.api.database.connector.SQLiteDatabaseConnector;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NexAuthSQLiteDatabaseProvider extends NexAuthSQLDatabaseProvider {
    public NexAuthSQLiteDatabaseProvider(SQLiteDatabaseConnector connector, AuthenticNexAuth<?, ?> plugin) {
        super(connector, plugin);
    }

    @Override
    protected List<String> getColumnNames(Connection connection) throws SQLException {
        var columns = new ArrayList<String>();

        var rs = connection.prepareStatement("PRAGMA table_info(librepremium_data)").executeQuery();

        while (rs.next()) {
            columns.add(rs.getString("name"));
        }

        return columns;
    }

    @Override
    protected String getIgnoreSyntax() {
        return "OR IGNORE";
    }

    @Override
    protected String addUnique(String column) {
        return "CREATE UNIQUE INDEX %s_index ON librepremium_data(%s);".formatted(column, column);
    }
}

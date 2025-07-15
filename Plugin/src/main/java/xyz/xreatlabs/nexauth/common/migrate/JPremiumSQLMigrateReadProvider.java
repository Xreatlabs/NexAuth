/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.migrate;

import xyz.xreatlabs.nexauth.api.Logger;
import xyz.xreatlabs.nexauth.api.crypto.HashedPassword;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.database.connector.SQLDatabaseConnector;
import xyz.xreatlabs.nexauth.common.database.AuthenticUser;
import xyz.xreatlabs.nexauth.common.util.CryptoUtil;
import xyz.xreatlabs.nexauth.common.util.GeneralUtil;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;

public class JPremiumSQLMigrateReadProvider extends SQLMigrateReadProvider {

    public JPremiumSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var uniqueIdString = rs.getString("uniqueId");
                    var premiumIdString = rs.getString("premiumId");
                    var lastNickname = rs.getString("lastNickname");
                    var lastSeen = rs.getTimestamp("lastSeen");
                    var firstSeen = rs.getTimestamp("firstSeen");
                    var rawPassword = rs.getString("hashedPassword");

                    if (lastNickname == null) continue; //Yes this may happen
                    var split = rawPassword == null ? null : rawPassword.split("\\$");

                    HashedPassword password = rawPassword == null ? null : switch (split[0]) {
                        case "SHA256" -> new HashedPassword(
                                split[2],
                                split[1],
                                "SHA-256"
                        );
                        case "SHA512" -> new HashedPassword(
                                split[2],
                                split[1],
                                "SHA-512"
                        );
                        case "BCRYPT" -> CryptoUtil.convertFromBCryptRaw(rawPassword.replace("BCRYPT", "$2a"));
                        default -> {
                            logger.error("User %s has invalid algorithm %s, omitting".formatted(lastNickname, split[0]));
                            yield null;
                        }
                    };

                    users.add(new AuthenticUser(
                            GeneralUtil.fromUnDashedUUID(uniqueIdString),
                            premiumIdString == null ? null : GeneralUtil.fromUnDashedUUID(premiumIdString),
                            password,
                            lastNickname,
                            firstSeen,
                            lastSeen,
                            null,
                            null,
                            null,
                            null,
                            null
                    ));

                } catch (Exception e) {
                    logger.error("Failed to read user from JPremium db, omitting");
                }
            }

            return users;

        });
    }
}

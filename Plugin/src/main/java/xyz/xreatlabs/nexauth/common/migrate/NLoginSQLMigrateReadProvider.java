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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

public class NLoginSQLMigrateReadProvider extends SQLMigrateReadProvider {
    public NLoginSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var uniqueIdString = rs.getString("unique_id");
                    var premiumIdString = rs.getString("mojang_id");
                    var lastNickname = rs.getString("last_name");
                    var lastSeen = rs.getTimestamp("last_seen");
                    var firstSeen = rs.getTimestamp("creation_date");
                    var rawPassword = rs.getString("password");
                    var ip = rs.getString("last_ip");

                    if (lastNickname == null) continue; //Yes this may happen
                    if (uniqueIdString == null) continue; //Yes this may happen

                    HashedPassword password = null;

                    if (rawPassword != null) {
                        if (rawPassword.startsWith("$SHA512$")) {
                            var split = rawPassword.substring(8).split("\\$");
                            password = new HashedPassword(
                                    split[0],
                                    split[1],
                                    "SHA-512"
                            );
                        }else if(rawPassword.startsWith("$2a$")){
                            password = CryptoUtil.convertFromBCryptRaw(rawPassword);
                        }else{
                            logger.error("User %s has invalid algorithm %s, omitting".formatted(lastNickname, rawPassword));
                            continue;
                        }

                    }

                    users.add(new AuthenticUser(
                            GeneralUtil.fromUnDashedUUID(uniqueIdString),
                            premiumIdString == null ? null : GeneralUtil.fromUnDashedUUID(premiumIdString),
                            password,
                            lastNickname,
                            firstSeen,
                            lastSeen,
                            null,
                            ip,
                            Timestamp.from(Instant.EPOCH),
                            null,
                            null
                    ));

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error while reading user from database");
                }
            }

            return users;
        });
    }
}

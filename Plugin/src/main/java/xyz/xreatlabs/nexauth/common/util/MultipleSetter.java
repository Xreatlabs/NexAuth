/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Used for setting the same values multiple times, for example in INSERT ... ON DUPLICATE KEY UPDATE ...
 *
 * @author kyngs
 */
public class MultipleSetter {

    private final Map<Integer, Object> toSet;

    public MultipleSetter() {
        toSet = new HashMap<>();
    }

    public void set(int place, Object object) {
        if (object instanceof LocalDateTime) {
            toSet.put(place, Timestamp.valueOf((LocalDateTime) object));
        } else {
            toSet.put(place, object);
        }
    }

    public void apply(PreparedStatement ps, int count) throws SQLException {
        var counter = 0;
        for (int i = 0; i < count; i++) {
            for (Map.Entry<Integer, Object> entry : toSet.entrySet()) {
                ps.setObject(entry.getKey() + counter, entry.getValue());
            }
            counter += toSet.size();
        }
    }
}

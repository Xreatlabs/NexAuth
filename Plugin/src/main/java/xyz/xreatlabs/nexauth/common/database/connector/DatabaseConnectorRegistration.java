/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.database.connector;

import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;
import xyz.xreatlabs.nexauth.api.util.ThrowableFunction;

/**
 * This class is used to register new database connectors.
 *
 * @param <E>     The common exception type thrown by the database.
 * @param <C>     The type of the database connector.
 * @param factory The factory used to create the connector. The string parameter is the configuration prefix.
 * @param id
 */
public record DatabaseConnectorRegistration<E extends Exception, C extends DatabaseConnector<E, ?>>(
        ThrowableFunction<String, C, E> factory, Class<?> configClass, String id) {
}

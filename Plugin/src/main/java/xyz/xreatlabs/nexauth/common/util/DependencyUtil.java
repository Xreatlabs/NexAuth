/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.util;

import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import xyz.xreatlabs.nexauth.api.Logger;

import java.util.Collection;

public class DependencyUtil {

    public static void downloadDependencies(Logger logger, LibraryManager libraryManager, Collection<String> customRepositories, Collection<Library> customDependencies) {
        logger.info("Loading libraries...");

        libraryManager.configureFromJSON();
    }

}

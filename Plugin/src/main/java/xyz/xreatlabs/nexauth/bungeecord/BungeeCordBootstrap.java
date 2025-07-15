/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.bungeecord;

import net.byteflux.libby.BungeeLibraryManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.xreatlabs.nexauth.api.provider.NexAuthProvider;

public class BungeeCordBootstrap extends Plugin implements NexAuthProvider<ProxiedPlayer, ServerInfo> {

    private BungeeCordNexAuth libreLogin;

    @Override
    public void onLoad() {
        var libraryManager = new BungeeLibraryManager(this);

        getLogger().info("Loading libraries...");

        libraryManager.configureFromJSON();

        libreLogin = new BungeeCordNexAuth(this);
    }

    @Override
    public void onEnable() {
        libreLogin.enable();
    }

    @Override
    public void onDisable() {
        libreLogin.disable();
    }

    @Override
    public BungeeCordNexAuth getNexAuth() {
        return libreLogin;
    }

}

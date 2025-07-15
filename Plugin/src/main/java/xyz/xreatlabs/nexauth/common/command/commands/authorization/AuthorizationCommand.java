/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.command.commands.authorization;

import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.command.Command;
import xyz.xreatlabs.nexauth.common.command.InvalidCommandArgument;

public class AuthorizationCommand<P> extends Command<P> {

    public AuthorizationCommand(AuthenticNexAuth<P, ?> premium) {
        super(premium);
    }

    protected void checkUnauthorized(P player) {
        if (getAuthorizationProvider().isAuthorized(player)) {
            throw new InvalidCommandArgument(getMessage("error-already-authorized"));
        }
    }

}

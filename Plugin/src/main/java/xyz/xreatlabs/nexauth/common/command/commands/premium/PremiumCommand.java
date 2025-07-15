/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.command.commands.premium;

import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.command.InvalidCommandArgument;
import xyz.xreatlabs.nexauth.common.command.commands.authorization.AuthorizationCommand;

public class PremiumCommand<P> extends AuthorizationCommand<P> {
    public PremiumCommand(AuthenticNexAuth<P, ?> premium) {
        super(premium);
    }

    protected void checkCracked(User user) {
        if (user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-cracked"));
    }

    protected void checkPremium(User user) {
        if (!user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-premium"));
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.command.commands.mail;

import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.command.Command;
import xyz.xreatlabs.nexauth.common.mail.AuthenticEMailHandler;

public class EMailCommand<P> extends Command<P> {

    protected final AuthenticEMailHandler mailHandler;

    public EMailCommand(AuthenticNexAuth<P, ?> plugin) {
        super(plugin);
        mailHandler = plugin.getEmailHandler();
        assert mailHandler != null;
    }
}

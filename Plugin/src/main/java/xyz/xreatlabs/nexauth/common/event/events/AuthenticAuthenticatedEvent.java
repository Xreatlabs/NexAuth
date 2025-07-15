/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.xreatlabs.nexauth.api.NexAuthPlugin;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.event.events.AuthenticatedEvent;
import xyz.xreatlabs.nexauth.common.event.AuthenticPlayerBasedEvent;

public class AuthenticAuthenticatedEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements AuthenticatedEvent<P, S> {

    private final AuthenticationReason reason;

    public AuthenticAuthenticatedEvent(@Nullable User user, P player, NexAuthPlugin<P, S> plugin, AuthenticationReason reason) {
        super(user, player, plugin);
        this.reason = reason;
    }

    @Override
    public AuthenticationReason getReason() {
        return reason;
    }
}

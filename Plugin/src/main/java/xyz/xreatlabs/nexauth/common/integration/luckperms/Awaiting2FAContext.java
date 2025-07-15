/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.integration.luckperms;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;

public class Awaiting2FAContext<P> implements ContextCalculator<P> {

    private final AuthenticNexAuth<P, ?> plugin;

    public Awaiting2FAContext(AuthenticNexAuth<P, ?> plugin) {
        this.plugin = plugin;
    }

    private static final String KEY = "nexauth-awaiting2fa";

    @Override
    public void calculate(@NonNull P player, @NonNull ContextConsumer consumer) {
        consumer.accept(KEY, Boolean.toString(plugin.getAuthorizationProvider().isAwaiting2FA(player)));
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(KEY, "true")
                .add(KEY, "false")
                .build();
    }
}

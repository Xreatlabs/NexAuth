/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.premium;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import net.kyori.adventure.audience.Audience;
import org.bstats.charts.CustomChart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.Logger;
import xyz.xreatlabs.nexauth.api.PlatformHandle;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.premium.PremiumException;
import xyz.xreatlabs.nexauth.api.premium.PremiumUser;
import xyz.xreatlabs.nexauth.api.server.ServerPing;
import xyz.xreatlabs.nexauth.api.util.ThrowableFunction;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.image.AuthenticImageProjector;
import xyz.xreatlabs.nexauth.common.util.CancellableTask;

import java.lang.reflect.Field;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticPremiumProviderTest {

    private final Locale defaultLocale = Locale.getDefault();

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    void normalizesNamesWithStableLocale() throws Exception {
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        var observedNames = new ArrayList<String>();
        var provider = providerWithFetchers(name -> {
            observedNames.add(name);
            return null;
        });

        provider.getUserForName("ILKER");

        assertEquals(List.of("ilker"), observedNames);
    }

    @Test
    void failsClosedWhenAuthoritativeLookupFailsAndFallbacksMiss() throws Exception {
        var provider = providerWithFetchers(
                name -> {
                    throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, "Mojang unavailable");
                },
                name -> null,
                name -> null
        );

        var exception = assertThrows(PremiumException.class, () -> provider.getUserForName("Steve"));

        assertEquals(PremiumException.Issue.SERVER_EXCEPTION, exception.getIssue());
    }

    @Test
    void allowsFallbackPremiumMatchAfterAuthoritativeLookupFails() throws Exception {
        var premium = new PremiumUser(UUID.randomUUID(), "Steve", false);
        var provider = providerWithFetchers(
                name -> {
                    throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, "Mojang unavailable");
                },
                name -> premium
        );

        assertSame(premium, provider.getUserForName("Steve"));
    }

    @SafeVarargs
    private static AuthenticPremiumProvider providerWithFetchers(ThrowableFunction<String, PremiumUser, PremiumException>... fetchers) throws Exception {
        var provider = new AuthenticPremiumProvider(new TestPlugin());
        Field field = AuthenticPremiumProvider.class.getDeclaredField("fetchers");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var configuredFetchers = (List<ThrowableFunction<String, PremiumUser, PremiumException>>) field.get(provider);
        configuredFetchers.clear();
        configuredFetchers.addAll(List.of(fetchers));
        return provider;
    }

    private static final class TestPlugin extends AuthenticNexAuth<Object, Object> {
        private static final Logger LOGGER = (Logger) java.lang.reflect.Proxy.newProxyInstance(
                Logger.class.getClassLoader(),
                new Class[]{Logger.class},
                (proxy, method, args) -> null
        );

        private static final PlatformHandle<Object, Object> PLATFORM = new PlatformHandle<>() {
            @Override public Audience getAudienceForPlayer(Object player) { return null; }
            @Override public UUID getUUIDForPlayer(Object player) { return UUID.randomUUID(); }
            @Override public CompletableFuture<Throwable> movePlayer(Object player, Object to) { return CompletableFuture.completedFuture(null); }
            @Override public void kick(Object player, net.kyori.adventure.text.Component reason) { }
            @Override public Object getServer(String name, boolean limbo) { return null; }
            @Override public Class<Object> getServerClass() { return Object.class; }
            @Override public Class<Object> getPlayerClass() { return Object.class; }
            @Override public String getIP(Object player) { return "127.0.0.1"; }
            @Override public ServerPing ping(Object server) { return null; }
            @Override public Collection<Object> getServers() { return List.of(); }
            @Override public String getServerName(Object server) { return "server"; }
            @Override public int getConnectedPlayers(Object server) { return 0; }
            @Override public String getPlayersServerName(Object player) { return "server"; }
            @Override public String getPlayersVirtualHost(Object player) { return "localhost"; }
            @Override public String getUsernameForPlayer(Object player) { return "player"; }
            @Override public String getPlatformIdentifier() { return "test"; }
            @Override public ProxyData getProxyData() { return new ProxyData("test", List.of(), List.of(), List.of(), List.of()); }
        };

        @Override protected PlatformHandle<Object, Object> providePlatformHandle() { return PLATFORM; }
        @Override protected Logger provideLogger() { return LOGGER; }
        @Override public Logger getLogger() { return LOGGER; }
        @Override public InputStream getResourceAsStream(String name) { return null; }
        @Override public File getDataFolder() { return new File("build/test-nexauth"); }
        @Override public String getVersion() { return "0.0.0-test"; }
        @Override public boolean isPresent(UUID uuid) { return false; }
        @Override public boolean multiProxyEnabled() { return false; }
        @Override public Object getPlayerForUUID(UUID uuid) { return null; }
        @Override public CommandManager<?, ?, ?, ?, ?, ?> provideManager() { return null; }
        @Override public Object getPlayerFromIssuer(CommandIssuer issuer) { return null; }
        @Override public void authorize(Object player, User user, Audience audience) { }
        @Override public CancellableTask delay(Runnable runnable, long delayInMillis) { return () -> { }; }
        @Override public CancellableTask repeat(Runnable runnable, long delayInMillis, long repeatInMillis) { return () -> { }; }
        @Override public boolean pluginPresent(String pluginName) { return false; }
        @Override protected AuthenticImageProjector<Object, Object> provideImageProjector() { return null; }
        @Override protected void initMetrics(CustomChart... charts) { }
        @Override public Audience getAudienceFromIssuer(CommandIssuer issuer) { return null; }
    }
}

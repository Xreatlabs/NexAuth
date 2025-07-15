/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.integration;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.lang.reflect.Method;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.data.InfoForwarding;
import xyz.xreatlabs.nexauth.common.integration.nanolimbo.NanoLimboIntegration;

public class VelocityNanoLimboIntegration extends NanoLimboIntegration<RegisteredServer> {

    private final ClassLoader classLoader;
    private final ProxyServer proxyServer;

    public VelocityNanoLimboIntegration(ProxyServer proxyServer, String portRange) {
        super(portRange);
        this.classLoader = NanoLimbo.class.getClassLoader();
        this.proxyServer = proxyServer;
    }

    @Override
    public RegisteredServer createLimbo(String serverName) {
        InetSocketAddress address = findLocalAvailableAddress().orElseThrow(() -> new IllegalStateException("Cannot find available port for limbo server!"));
        LimboServer server = createLimboServer(address);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxyServer.registerServer(new ServerInfo(serverName, address));
    }

    @Override
    protected InfoForwarding createForwarding() {
        // Use reflection to safely access internal configuration
        try {
            Object velocityConfiguration = proxyServer.getConfiguration();
            Class<?> configClass = velocityConfiguration.getClass();
            
            // Try to get the forwarding mode using reflection
            Object forwardingMode = configClass.getMethod("getPlayerInfoForwardingMode").invoke(velocityConfiguration);
            String forwardingModeName = forwardingMode.toString();
            
            switch (forwardingModeName) {
                case "NONE":
                    return FORWARDING_FACTORY.none();
                case "LEGACY":
                    return FORWARDING_FACTORY.legacy();
                case "MODERN":
                    byte[] secret = (byte[]) configClass.getMethod("getForwardingSecret").invoke(velocityConfiguration);
                    return FORWARDING_FACTORY.modern(secret);
                case "BUNGEEGUARD":
                    byte[] bungeeSecret = (byte[]) configClass.getMethod("getForwardingSecret").invoke(velocityConfiguration);
                    return FORWARDING_FACTORY.bungeeGuard(Collections.singleton(new String(bungeeSecret, StandardCharsets.UTF_8)));
                default:
                    // Fallback to NONE if unknown mode
                    return FORWARDING_FACTORY.none();
            }
        } catch (Exception e) {
            // If reflection fails, fallback to NONE forwarding
            // This ensures compatibility with future Velocity versions
            return FORWARDING_FACTORY.none();
        }
    }

    @Override
    protected ClassLoader classLoader() {
        return classLoader;
    }

}

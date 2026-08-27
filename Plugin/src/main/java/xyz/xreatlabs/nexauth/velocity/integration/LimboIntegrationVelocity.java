/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.integration;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Optional;
import xyz.xreatlabs.nexauth.api.integration.LimboIntegration;
import xyz.xreatlabs.nexauth.velocity.limbo.Forwarding;
import xyz.xreatlabs.nexauth.velocity.limbo.LimboServer;
import xyz.xreatlabs.nexauth.velocity.limbo.LimboSettings;

public class LimboIntegrationVelocity implements LimboIntegration<RegisteredServer> {

  private static final String MOTD = "NexAuth Limbo";
  private static final int MAX_PLAYERS = 100;
  private static final int KEEP_ALIVE_INTERVAL_SECONDS = 5;
  private static final int READ_TIMEOUT_SECONDS = 30;

  private final ProxyServer proxyServer;
  private final int portMin;
  private final int portMax;

  public LimboIntegrationVelocity(ProxyServer proxyServer, String portRange) {
    this.proxyServer = proxyServer;
    String[] split = portRange.split("-");
    this.portMin = Integer.parseInt(split[0]);
    this.portMax = Integer.parseInt(split[1]);
  }

  @Override
  public RegisteredServer createLimbo(String serverName) {
    InetSocketAddress address =
        findLocalAvailableAddress()
            .orElseThrow(
                () -> new IllegalStateException("Cannot find available port for limbo server!"));

    LimboServer server =
        new LimboServer(
            new LimboSettings(
                MAX_PLAYERS,
                MOTD,
                KEEP_ALIVE_INTERVAL_SECONDS,
                READ_TIMEOUT_SECONDS,
                resolveForwardingMode(),
                forwardingSecret()));
    try {
      server.start(address);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot start limbo server on " + address, e);
    }

    return proxyServer.registerServer(new ServerInfo(serverName, address));
  }

  private Forwarding.Mode resolveForwardingMode() {
    // Use reflection to safely access internal configuration
    try {
      Object velocityConfiguration = proxyServer.getConfiguration();
      Class<?> configClass = velocityConfiguration.getClass();

      Object forwardingMode =
          configClass.getMethod("getPlayerInfoForwardingMode").invoke(velocityConfiguration);
      String forwardingModeName = forwardingMode.toString();

      switch (forwardingModeName) {
        case "MODERN":
          return Forwarding.Mode.MODERN;
        case "LEGACY":
          return Forwarding.Mode.LEGACY;
        case "BUNGEEGUARD":
          return Forwarding.Mode.BUNGEEGUARD;
        case "NONE":
        default:
          // Fallback to NONE if unknown mode
          return Forwarding.Mode.NONE;
      }
    } catch (Exception e) {
      // If reflection fails, fallback to NONE forwarding
      // This ensures compatibility with future Velocity versions
      return Forwarding.Mode.NONE;
    }
  }

  private byte[] forwardingSecret() {
    try {
      Object velocityConfiguration = proxyServer.getConfiguration();
      return (byte[])
          velocityConfiguration
              .getClass()
              .getMethod("getForwardingSecret")
              .invoke(velocityConfiguration);
    } catch (Exception e) {
      return new byte[0];
    }
  }

  private Optional<InetSocketAddress> findLocalAvailableAddress() {
    for (int port = portMin; port <= portMax; port++) {
      try (ServerSocket ignored = new ServerSocket(port)) {
        return Optional.of(new InetSocketAddress(port));
      } catch (IOException ignored) {
      }
    }
    return Optional.empty();
  }
}

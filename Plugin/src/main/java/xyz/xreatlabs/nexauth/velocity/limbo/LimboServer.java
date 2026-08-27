/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LimboServer {

  private final LimboSettings settings;
  private final Set<LimboConnection> connections = ConcurrentHashMap.newKeySet();

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  public LimboServer(LimboSettings settings) {
    this.settings = settings;
  }

  public void start(InetSocketAddress addr) throws Exception {
    ChannelFactory<? extends ServerChannel> channelFactory;

    if (Epoll.isAvailable()) {
      bossGroup = new MultiThreadIoEventLoopGroup(1, EpollIoHandler.newFactory());
      workerGroup = new MultiThreadIoEventLoopGroup(2, EpollIoHandler.newFactory());
      channelFactory = EpollServerSocketChannel::new;
    } else {
      bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
      workerGroup = new MultiThreadIoEventLoopGroup(2, NioIoHandler.newFactory());
      channelFactory = NioServerSocketChannel::new;
    }

    new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channelFactory(channelFactory)
        .childHandler(new LimboChannelInitializer(this))
        .childOption(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_REUSEADDR, true)
        .localAddress(addr)
        .bind()
        .sync();
  }

  public int connectionCount() {
    return connections.size();
  }

  public LimboSettings settings() {
    return settings;
  }

  /** Shared logger for pipeline handlers; the platform provides the slf4j backend. */
  static Logger log() {
    return LoggerFactory.getLogger("nexauth-limbo");
  }

  void add(LimboConnection c) {
    connections.add(c);
  }

  void remove(LimboConnection c) {
    connections.remove(c);
  }

  Set<LimboConnection> connections() {
    return Collections.unmodifiableSet(connections);
  }
}

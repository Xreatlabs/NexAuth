/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import ua.nanit.limbo.connection.pipeline.*;
import ua.nanit.limbo.server.LimboServer;

public class ClientChannelInitializer extends ChannelInitializer<Channel> {

  private final LimboServer server;

  public ClientChannelInitializer(LimboServer server) {
    this.server = server;
  }

  @Override
  protected void initChannel(Channel channel) {
    ChannelPipeline pipeline = channel.pipeline();

    PacketDecoder decoder = new PacketDecoder();
    PacketEncoder encoder = new PacketEncoder();
    ClientConnection connection = new ClientConnection(channel, server, decoder, encoder);

    pipeline.addLast(
        "timeout",
        new ReadTimeoutHandler(server.getConfig().getReadTimeout(), TimeUnit.MILLISECONDS));
    pipeline.addLast("frame_decoder", new VarIntFrameDecoder());
    pipeline.addLast("frame_encoder", new VarIntLengthEncoder());

    if (server.getConfig().isUseTrafficLimits()) {
      pipeline.addLast(
          "traffic_limit",
          new ChannelTrafficHandler(
              server.getConfig().getMaxPacketSize(),
              server.getConfig().getInterval(),
              server.getConfig().getMaxPacketRate()));
    }

    pipeline.addLast("decoder", decoder);
    pipeline.addLast("encoder", encoder);
    pipeline.addLast("handler", connection);
  }
}

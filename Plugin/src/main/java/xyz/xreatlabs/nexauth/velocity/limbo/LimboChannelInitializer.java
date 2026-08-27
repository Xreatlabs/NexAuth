/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class LimboChannelInitializer extends ChannelInitializer<Channel> {

  private final LimboServer server;

  public LimboChannelInitializer(LimboServer server) {
    this.server = server;
  }

  @Override
  protected void initChannel(Channel channel) {
    ChannelPipeline pipeline = channel.pipeline();

    pipeline.addLast("timeout", new ReadTimeoutHandler(server.settings().readTimeoutSeconds()));
    pipeline.addLast("frame_decoder", new VarIntFrameDecoder());
    pipeline.addLast("frame_encoder", new VarIntLengthEncoder());
    pipeline.addLast("handler", new LimboConnection(server));
  }
}

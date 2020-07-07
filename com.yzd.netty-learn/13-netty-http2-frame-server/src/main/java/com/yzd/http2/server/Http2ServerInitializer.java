package com.yzd.http2.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2Settings;

public class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        p.addLast(Http2FrameCodecBuilder.forServer()
                .autoAckPingFrame(false)
                .autoAckSettingsFrame(false)
                .initialSettings(Http2Settings.defaultSettings())
                .build(), new Http2ServerHandler());
    }
}

package com.yzd.http2.proxy;

import com.yzd.http2.server.Http2ServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2Settings;

public class Http2ProxyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        p.addLast(Http2FrameCodecBuilder.forServer()
                //.autoAckPingFrame(false)
                //.autoAckSettingsFrame(false)
                .decoupleCloseAndGoAway(false)
                .validateHeaders(false)
                .initialSettings(Http2Settings.defaultSettings())
                .build(), new Http2ProxyServerHandler());
    }
}

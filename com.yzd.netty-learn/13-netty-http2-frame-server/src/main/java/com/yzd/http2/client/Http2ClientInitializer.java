package com.yzd.http2.client;

import com.yzd.http2.server.Http2ServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2Settings;

/**
 * @author yaozh
 */
public class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        p.addLast(Http2FrameCodecBuilder.forClient()
                .autoAckPingFrame(false)
                .autoAckSettingsFrame(false)
                .decoupleCloseAndGoAway(false)
                .validateHeaders(false)
                .initialSettings(Http2Settings.defaultSettings())
                .build(), new Http2ClientHandler());
    }
}

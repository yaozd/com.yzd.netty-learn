package com.yzd.http2.proxy;

import com.yzd.http2.client.Http2ClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2Settings;

/**
 * @author yaozh
 */
public class Http2ProxyClientInitializer extends ChannelInitializer<SocketChannel> {
    private final Channel serverChannel;

    public Http2ProxyClientInitializer(Channel serverChannel){
        this.serverChannel=serverChannel;
    }
    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        p.addLast(Http2FrameCodecBuilder.forClient()
                //.autoAckPingFrame(false)
                //.autoAckSettingsFrame(false)
                .decoupleCloseAndGoAway(false)
                .validateHeaders(false)
                .initialSettings(Http2Settings.defaultSettings())
                .build(), new Http2ProxyClientHandler(serverChannel));
    }
}

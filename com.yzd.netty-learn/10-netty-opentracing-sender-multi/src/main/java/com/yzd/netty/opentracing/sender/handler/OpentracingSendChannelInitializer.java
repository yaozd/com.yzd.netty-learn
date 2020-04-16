package com.yzd.netty.opentracing.sender.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
public class OpentracingSendChannelInitializer extends ChannelInitializer<Channel> {
    public static final String HTTP_CLIENT_HANDLER_NAME = "OpentracingSendChannelHandler";
    private final SslContext sslCtx;

    public OpentracingSendChannelInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // Enable HTTPS if necessary.
        if (sslCtx != null) {
            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
        }
        ch.pipeline().addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
        ch.pipeline().addLast(new HttpServerKeepAliveHandler());
        ch.pipeline().addLast(new HttpClientCodec());
        // Remove the following line if you don't want automatic content decompression.
        ch.pipeline().addLast(new HttpContentDecompressor());
        // Uncomment the following line if you don't want to handle HttpContents.
        ch.pipeline().addLast(new HttpObjectAggregator(1048576));
        ch.pipeline().addLast(HTTP_CLIENT_HANDLER_NAME, new OpentracingSendChannelHandler());
    }
}

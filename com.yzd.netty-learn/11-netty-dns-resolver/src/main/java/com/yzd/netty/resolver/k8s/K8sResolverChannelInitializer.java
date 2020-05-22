package com.yzd.netty.resolver.k8s;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
public class K8sResolverChannelInitializer extends ChannelInitializer<Channel> {
    public static final String HTTP_CLIENT_HANDLER_NAME = "OpentracingSendChannelHandler";
    private final SslContext sslCtx;
    private final RequestType requestType;
    private final K8sResolverProvider resolverProvider;
    private final URI uri;

    public K8sResolverChannelInitializer(K8sResolverProvider resolverProvider, SslContext sslCtx, RequestType requestType, URI uri) {
        this.resolverProvider = resolverProvider;
        this.sslCtx = sslCtx;
        this.requestType = requestType;
        this.uri=uri;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // Enable HTTPS if necessary.
        if (sslCtx != null) {
            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
        }
        ch.pipeline().addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
        //ch.pipeline().addLast(new HttpResponseDecoder());
        // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
        //ch.pipeline().addLast(new HttpRequestEncoder());
        ch.pipeline().addLast(new HttpClientCodec());
        //ch.pipeline().addLast(new HttpObjectAggregator(655350000));
        ch.pipeline().addLast(new K8sResolverChannelHandler(resolverProvider,requestType,uri));
    }
}

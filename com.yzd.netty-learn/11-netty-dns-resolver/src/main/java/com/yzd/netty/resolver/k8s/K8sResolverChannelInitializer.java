package com.yzd.netty.resolver.k8s;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.yzd.netty.resolver.k8s.K8sResolverProvider.READER_IDLE_TIME;
import static com.yzd.netty.resolver.k8s.K8sResolverProvider.WRITER_IDLE_TIME;
import static com.yzd.netty.resolver.k8s.RequestType.QUERY;
import static com.yzd.netty.resolver.k8s.RequestType.WATCH;

/**
 * @Author: yaozh
 * @Description:
 */
public class K8sResolverChannelInitializer extends ChannelInitializer<Channel> {
    private final SslContext sslCtx;
    private final RequestType requestType;
    private final K8sResolverProvider resolverProvider;
    private final URI uri;

    public K8sResolverChannelInitializer(
            K8sResolverProvider resolverProvider,
            SslContext sslCtx, RequestType requestType, URI uri) {
        this.resolverProvider = resolverProvider;
        this.sslCtx = sslCtx;
        this.requestType = requestType;
        this.uri = uri;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // Enable HTTPS if necessary.
        if (sslCtx != null) {
            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
        }
        ch.pipeline().addLast(
                new IdleStateHandler(READER_IDLE_TIME, WRITER_IDLE_TIME, 0, TimeUnit.MILLISECONDS));
        if (QUERY.equals(requestType)) {
            ch.pipeline().addLast(new HttpClientCodec());
            //10MB
            ch.pipeline().addLast(new HttpObjectAggregator(83886080));
            ch.pipeline().addLast(new K8sResolverQueryChannelHandler(resolverProvider, requestType, uri));
        }
        if (WATCH.equals(requestType)) {
            ch.pipeline().addLast(new K8sWatchHttpResponseDecoder());
            ch.pipeline().addLast(new HttpRequestEncoder());
            ch.pipeline().addLast(new K8sResolverWatchChannelHandler(resolverProvider, requestType, uri));
        }
    }
}

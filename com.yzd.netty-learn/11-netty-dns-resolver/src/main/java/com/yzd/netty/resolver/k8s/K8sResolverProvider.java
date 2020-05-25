package com.yzd.netty.resolver.k8s;

import com.google.common.collect.Maps;
import com.yzd.netty.resolver.BaseResolverProvider;
import com.yzd.netty.resolver.config.TargetNode;
import com.yzd.netty.resolver.utils.HttpRequestUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yzd.netty.resolver.k8s.RequestType.QUERY;
import static com.yzd.netty.resolver.k8s.RequestType.WATCH;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverProvider extends BaseResolverProvider {
    private static final int MAX_FAST_CONNECTION_COUNT = 10;
    public boolean parseSuccess = true;
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int connectionFailCount = 0;
    private volatile ScheduledFuture<?> scheduledFuture;

    protected K8sResolverProvider(TargetNode targetNode) {
        super(targetNode);
    }


    public void query() {
        log.info("Query!");
        URI uri = createUri("http://192.168.56.102:8080/api/v1/namespaces/default/endpoints/my-nginx");
        Bootstrap b = getBootstrap();
        b.handler(new K8sResolverChannelInitializer(this, getSslContext(uri.getScheme()), QUERY, uri));
        b.connect(uri.getHost(), getPort(uri)).addListener(new ConnectFutureListener(this, uri));
    }

    private Bootstrap getBootstrap() {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.SO_REUSEADDR, true);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);
        return b;
    }

    public void watch() {
        log.info("Watch!");
        URI uri = createUri("http://192.168.56.102:8080/api/v1/watch/namespaces/default/endpoints/my-nginx");
        Bootstrap b = getBootstrap();
        b.handler(new K8sResolverChannelInitializer(this, getSslContext(uri.getScheme()), WATCH, uri));
        b.connect(uri.getHost(), getPort(uri)).addListener(new ConnectFutureListener(this, uri));
    }

    @Override
    public void cancel() {

    }

    public void reconnection() {
        scheduledFuture = workerGroup.schedule(() -> {
            try {
                query();
            } catch (Exception e) {
                log.error("opentracing sender connection fail!", e);
            }

        }, getIntervalTime(), TimeUnit.SECONDS);
    }

    private Long getIntervalTime() {
        if (connectionFailCount > MAX_FAST_CONNECTION_COUNT) {
            return 3L;
        }
        if (!parseSuccess) {
            return 3L;
        }
        return 0L;
    }

    private SslContext getSslContext(String scheme) {
        if (HttpScheme.HTTP.toString().equalsIgnoreCase(scheme)) {
            return null;
        }
        // 不验证SERVER
        try {
            return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            log.error("SSLException !", e);
            throw new IllegalStateException(e);
        }
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            if (HttpScheme.HTTP.toString().equalsIgnoreCase(uri.getScheme())) {
                port = 80;
            } else if (HttpScheme.HTTPS.toString().equalsIgnoreCase(uri.getScheme())) {
                port = 443;
            }
        }
        return port;
    }

    private static class ConnectFutureListener implements GenericFutureListener<ChannelFuture> {

        private final K8sResolverProvider resolverProvider;
        private final URI uri;

        public ConnectFutureListener(K8sResolverProvider resolverProvider, URI uri) {
            this.resolverProvider = resolverProvider;
            this.uri = uri;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            Channel newChannel = future.channel();
            if (!future.isSuccess()) {
                log.warn("Connection fail! url:{},  fail count:{}", uri, resolverProvider.connectionFailCount);
                resolverProvider.connectionFailCount++;
                if (newChannel != null && newChannel.isOpen()) {
                    newChannel.flush().close();
                }
                resolverProvider.reconnection();
                return;
            }
            //reset connection fail count
            resolverProvider.connectionFailCount = 0;
            Map<CharSequence, Object> header = Maps.newHashMapWithExpectedSize(2);
            header.put(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            header.put(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
            DefaultFullHttpRequest request = HttpRequestUtil.createFullHttpRequest(uri, HttpVersion.HTTP_1_1, HttpMethod.GET, header);
            newChannel.writeAndFlush(request);
        }
    }


}

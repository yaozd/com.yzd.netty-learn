package com.yzd.netty.opentracing.sender;

import com.yzd.netty.opentracing.sender.handler.OpentracingSendChannelHandler;
import com.yzd.netty.opentracing.sender.handler.OpentracingSendChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yzd.netty.opentracing.sender.handler.OpentracingSendChannelInitializer.HTTP_CLIENT_HANDLER_NAME;

@Slf4j
public class OpentracingMultiSender {
    /**
     * 配置版本，当reload的时候更新配置版本信息
     *
     */
    @Getter
    private String version= UUID.randomUUID().toString();
    private final int clientSize;
    public final Map<Integer, NettyClient> channelMap = new HashMap<>();
    private EventLoopGroup workerGroup = new NioEventLoopGroup(1);
    private Bootstrap b = new Bootstrap();

    public OpentracingMultiSender(int clientSize) {
        this.clientSize = clientSize;
        for (int i = 0; i < clientSize; i++) {
            channelMap.put(i,new NettyClient());
        }
        init();
    }

    private void init() {
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        //TCP_NODELAY:设置这样做好的好处就是禁用nagle算法
        //Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送.但这不是重点, 关键是这个算法受TCP延迟确认影响, 会导致相继两次向连接发送请求包,
        //读数据时会有一个最多达500毫秒的延时.
        b.option(ChannelOption.TCP_NODELAY, true);
        //SO_REUSEADDR是让端口释放后立即就可以被再次使用
        //用于对TCP套接字处于TIME_WAIT状态下的socket，才可以重复绑定使用
        b.option(ChannelOption.SO_REUSEADDR, true);
        //SO_KEEPALIVE:设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文。
        b.option(ChannelOption.SO_KEEPALIVE, true);
        //如果不设置超时，连接会一直占用本地线程，端口，连接客户端一多，会导致本地端口用尽及CPU压力
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);
    }

    public NettyClient getClient(int id){
        return channelMap.get(id);
    }
    public Channel getChannel(int id) {
        //return channelMap.get(id);
        return null;
    }

    public void newChannel(int id) {
        channelMap.get(id).setStatus(NettyClient.ClientStatus.CONNECTING);
        //String url = "https://www.baidu.com/";
        String url = "http://127.0.0.1:8090/multi/client";
        URI uri = createUri(url);
        b.handler(new OpentracingSendChannelInitializer(getSslContext(uri.getScheme())));
        b.connect(uri.getHost(), getPort(uri)).addListener(new ConnectFutureListener(this, id));
    }

    private URI createUri(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (!HttpScheme.HTTP.toString().equalsIgnoreCase(uri.getScheme())
                && !HttpScheme.HTTPS.toString().equalsIgnoreCase(uri.getScheme())) {
            throw new UnsupportedOperationException("Only HTTP(S) is supported.");
        }
        return uri;
    }

    /**
     * Configure SSL context if necessary.
     *
     * @param scheme
     * @return
     * @throws SSLException
     */
    private SslContext getSslContext(String scheme) {
        if (HttpScheme.HTTP.toString().equalsIgnoreCase(scheme)) {
            return null;
        }
        // 不验证SERVER
        try {
            return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
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

        private final OpentracingMultiSender opentracingMultiSender;
        private final Integer channelId;
        private final NettyClient nettyClient;

        public ConnectFutureListener(OpentracingMultiSender opentracingMultiSender, Integer channelId) {
            this.opentracingMultiSender = opentracingMultiSender;
            this.channelId = channelId;
            nettyClient=opentracingMultiSender.channelMap.get(channelId);
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            log.info("===========connect");
            Channel newChannel = future.channel();
            if (!future.isSuccess()) {
                nettyClient.setStatus(NettyClient.ClientStatus.FAILURE);
                log.info("连接失败");
                if (newChannel != null && newChannel.isOpen()) {
                    newChannel.flush().close();
                }
                return;
            }
            log.info("连接成功：触发监听操作！");
            nettyClient.setStatus(NettyClient.ClientStatus.SUCCESS);
            nettyClient.setChannel(newChannel);
            OpentracingSendChannelHandler httpClientHandler = (OpentracingSendChannelHandler) newChannel.pipeline().get(HTTP_CLIENT_HANDLER_NAME);
            httpClientHandler.attachSender(opentracingMultiSender,channelId);
        }
    }
}

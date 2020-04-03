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
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.yzd.netty.opentracing.sender.handler.OpentracingSendChannelInitializer.HTTP_CLIENT_HANDLER_NAME;


/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class OpentracingSender {

    public OpentracingSender(String url) throws Exception {
        this.url = url;
        init();
        connection();
    }

    private static final int MAX_FAST_CONNECTION_COUNT = 10;
    private static final int MAX_SEND_RETRY_COUNT = 2;
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Bootstrap b = new Bootstrap();
    private Channel channel;
    private int connectionFailCount = 0;
    private String url;
    private CountDownLatch connectionAwaitLatch=new CountDownLatch(1);
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

    public void connection() throws Exception {
        if (StringUtil.isNullOrEmpty(url)) {
            return;
        }
        URI uri = createUri(url);
        b.handler(new OpentracingSendChannelInitializer(getSslContext(uri.getScheme())));
        b.connect(uri.getHost(), getPort(uri)).addListener(new ConnectFutureListener(this));
    }

    public void send() throws InterruptedException {
        if(StringUtil.isNullOrEmpty(url)){
            return;
        }
        for (int i = 0; i < MAX_SEND_RETRY_COUNT; i++) {
            if (isAvailable()) {
                log.info("send span data");
                channel.writeAndFlush("span data");
                return;
            }
            if(connectionFailCount>MAX_FAST_CONNECTION_COUNT){
                log.error("fast fail!");
                return;
            }
            connectionAwaitLatch.await(2,TimeUnit.SECONDS);
        }
    }

    private URI createUri(String url) throws URISyntaxException {
        URI uri = new URI(url);
        if (!HttpScheme.HTTP.toString().equalsIgnoreCase(uri.getScheme())
                && !HttpScheme.HTTPS.toString().equalsIgnoreCase(uri.getScheme())) {
            throw new UnsupportedOperationException("Only HTTP(S) is supported.");
        }
        return uri;
    }

    public boolean isAvailable() {
        if (channel == null) {
            connectionAwaitLatch=new CountDownLatch(1);
            return false;
        }
        return channel.isActive();
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

    public void reconnection() {
        workerGroup.schedule(() -> {
            log.info("reconnection");
            try {
                connection();
            } catch (Exception e) {
                log.error("opentracing sender connection fail!", e);
            }

        }, getIntervalTime(), TimeUnit.SECONDS);
    }

    private Long getIntervalTime() {
        if (connectionFailCount > MAX_FAST_CONNECTION_COUNT) {
            return 3L;
        }
        return 0L;
    }

    private static class ConnectFutureListener implements GenericFutureListener<ChannelFuture> {

        private final OpentracingSender opentracingSender;

        public ConnectFutureListener(OpentracingSender opentracingSender) {
            this.opentracingSender = opentracingSender;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            log.info("===========connect");
            Channel newChannel = future.channel();
            if (!future.isSuccess()) {
                log.info("连接失败");
                opentracingSender.connectionFailCount++;
                if (newChannel != null && newChannel.isOpen()) {
                    newChannel.flush().close();
                }
                opentracingSender.reconnection();
                return;
            }
            log.info("连接成功：触发监听操作！");
            opentracingSender.channel = newChannel;
            //reset connection fail count
            opentracingSender.connectionFailCount = 0;
            opentracingSender.connectionAwaitLatch.countDown();
            OpentracingSendChannelHandler httpClientHandler = (OpentracingSendChannelHandler) newChannel.pipeline().get(HTTP_CLIENT_HANDLER_NAME);
            httpClientHandler.attachSender(opentracingSender);
        }
    }


    /**
     * Configure SSL context if necessary.
     *
     * @param scheme
     * @return
     * @throws SSLException
     */
    private SslContext getSslContext(String scheme) throws SSLException {
        if (HttpScheme.HTTPS.toString().equalsIgnoreCase(scheme)) {
            return null;
        }
        // 不验证SERVER
        return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    }
}

package com.yzd.client;

import com.yzd.resolve.data.RequestData;
import com.yzd.resolve.data.RequestType;
import com.yzd.resolve.Resolver;
import com.yzd.resolve.data.TaskInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class NettyHttpClient {
    private static NettyHttpClient ourInstance = new NettyHttpClient();

    public static NettyHttpClient getInstance() {
        return ourInstance;
    }

    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Bootstrap b = new Bootstrap();
    private static final String HTTP_CLIENT_HANDLER_NAME = "HttpClientHandler";
    private NettyHttpClient() {
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        //如果不设置超时，连接会一直占用本地线程，端口，连接客户端一多，会导致本地端口用尽及CPU压力
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                //入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间格式
                //因为服务端设置的超时时间是5秒，所以设置4秒
                ch.pipeline().addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new HttpServerKeepAliveHandler());
                ch.pipeline().addLast(new HttpClientCodec());
                // Remove the following line if you don't want automatic content decompression.
                ch.pipeline().addLast(new HttpContentDecompressor());
                // Uncomment the following line if you don't want to handle HttpContents.
                ch.pipeline().addLast(new HttpObjectAggregator(1048576));
                ch.pipeline().addLast(HTTP_CLIENT_HANDLER_NAME,new HttpClientHandler());
            }
        });
    }

    public Channel getChannel(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        try {
            return b.connect(host, port).sync().channel();
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        return null;
    }

    public void writeData(RequestData requestData) {
        URI uri = RequestUtil.getUri(requestData);
        // 连接服务端
        b.connect(uri.getHost(), uri.getPort()).addListener(new ConnectFutureListener(requestData));
    }
    private static class ConnectFutureListener implements GenericFutureListener<ChannelFuture> {

        private final RequestData requestData;

        ConnectFutureListener(RequestData requestData) {
            this.requestData=requestData;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            System.out.println("===========connect");
            Channel newChannel = future.channel();
            if (!future.isSuccess()) {
                System.out.println("连接失败");
                if (newChannel != null && newChannel.isOpen()) {
                    newChannel.flush().close();
                }
                //TODO 任务连接失败:+1，就会频繁的建立连接。此时可通过任务连接次数增加任务调用的时间
                requestData.incrementConnectionFail();
                Resolver.getInstance().addRequestDataQueue(requestData);
                return;
            }
            System.out.println("连接成功：触发监听操作！");
            requestData.resetConnectionFail();
            HttpClientHandler httpClientHandler = (HttpClientHandler)newChannel.pipeline().get(HTTP_CLIENT_HANDLER_NAME);
            httpClientHandler.attachStream(requestData);
            URI uri = RequestUtil.getUri(requestData);
            newChannel.writeAndFlush(RequestUtil.getRequestPackage(uri));
        }
    }
}

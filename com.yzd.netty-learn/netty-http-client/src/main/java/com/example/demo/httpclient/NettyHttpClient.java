package com.example.demo.httpclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyHttpClient {
    private static NettyHttpClient ourInstance = new NettyHttpClient();

    public static NettyHttpClient getInstance() {
        return ourInstance;
    }

    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Bootstrap b = new Bootstrap();

    private NettyHttpClient() {
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                //入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间格式
                //因为服务端设置的超时时间是5秒，所以设置4秒
                ch.pipeline().addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                ch.pipeline().addLast(new HttpResponseDecoder());
                // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                ch.pipeline().addLast(new HttpRequestEncoder());
                ch.pipeline().addLast(new HttpServerKeepAliveHandler());
                ch.pipeline().addLast(new HttpClientInboundHandler());
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
}

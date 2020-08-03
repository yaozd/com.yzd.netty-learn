package com.yzd.client;

import com.yzd.server._MainServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @Author: yaozh
 * @Description:
 */
public class _MainClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MyChannelInitializer());
            ChannelFuture future = bootstrap.connect("localhost", _MainServer.PORT).sync();
            String remoteAddress = future.channel().remoteAddress().toString();
            System.out.println(remoteAddress);
            Object request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "www.baidu.com");
            future.channel().write(request);
            //如果不主动flush，则数据不会真实的发送出去，只是停留在wirte队列之中
            future.channel().flush();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

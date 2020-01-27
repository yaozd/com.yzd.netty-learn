package com.yzd.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author: yaozh
 * @Description:
 */
public class _MainServer {
    public static final int PORT = 8999;
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //通过loggingHandler可以更好的分析channel的生命周期
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MyChannelInitializer());
            ChannelFuture future = serverBootstrap.bind(PORT).sync();
            System.out.println("服务端启动成功,端口是:"+PORT);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
}
}

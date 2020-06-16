package com.yzd.http.server.tester;

import com.yzd.http.server.tester.server.HttpServerInitializer;
import com.yzd.http.server.tester.utils.NettyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.TCP_NODELAY;

/**
 * @Author: yaozh
 * @Description:
 */
public class NettyHttpServer {
    /**
     * Java 设置系统参数和运行参数
     * 系统参数的标准格式为：-Dargname=argvalue，多个参数之间用空格隔开，如果参数值中间有空格，则用引号括起来
     * 在程序中可以用 System.getProperty("propertyName") 获取对应参数值。
     * eg:
     * -Dport=8888
     * System.getProperty("port", "8081")
     */
    private static final int PORT = Integer.parseInt(System.getProperty("port", "8081"));

    public static void main(String[] args) throws Exception {
        initHttp(PORT);
    }

    private static void initHttp(int port) throws Exception {
        // Configure the server.
        EventLoopGroup bossGroup = NettyUtil.newBossEventLoopGroup();
        ((NioEventLoopGroup) bossGroup).setIoRatio(100);
        EventLoopGroup workerGroup = NettyUtil.newWorkerEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .option(SO_BACKLOG, 1000000)
                    //.option(SO_REUSEADDR, true)
                    //.childOption(SO_REUSEADDR, true)
                    .childOption(TCP_NODELAY, true)
                    //.option(SO_KEEPALIVE, true)
                    //.childOption(TCP_NODELAY, true)
                    //.childOption(AUTO_CLOSE, true)
                    //.childOption(SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());
            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}

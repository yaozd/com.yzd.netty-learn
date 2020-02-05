package com.yzd.tcpclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;

/**
 * @Author: yaozh
 * @Description:
 */
public class NettyTcpClient {
    private static NettyTcpClient ourInstance = new NettyTcpClient();

    public static NettyTcpClient getInstance() {
        return ourInstance;
    }

    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Bootstrap b = new Bootstrap();

    private NettyTcpClient() {
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.handler(new NettyTcpClientChannelInitializer());
    }

    public Channel getChannel(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        try {
            return b.connect(host, port).sync().channel();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
    }
}

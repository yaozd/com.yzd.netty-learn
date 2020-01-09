package com.yzd.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Title: NettyClient
 * Description:
 * Netty客户端  心跳测试
 * Version:1.0.0
 *
 * @author pancm
 * @date 2017年10月8日
 */
public class NettyClient {

    public static String host = "127.0.0.1";  //ip地址
    public static int port = 8081;          //端口
    /// 通过nio方式来接收连接和处理连接   
    private static EventLoopGroup group = new NioEventLoopGroup();
    private static Bootstrap b = new Bootstrap();
    private static Channel ch;

    /**
     * Netty创建全部都是实现自AbstractBootstrap。
     * 客户端的是Bootstrap，服务端的则是    ServerBootstrap。
     **/
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("服务端使用Netty-heartbeat的server即可");
        System.out.println("客户端成功启动...");
        b.group(group);
        b.channel(NioSocketChannel.class);
        b.handler(new NettyClientFilter());
        for (int i = 0; i < 1000; i++) {
            // 连接服务端
            b.connect(host, port).addListener(new ConnectFutureListener("this is listener"));
        }
        // 连接服务端
        b.connect(host, port).addListener(new ConnectFutureListener("this is listener"));
    }
    private static class ConnectFutureListener implements GenericFutureListener<ChannelFuture> {

        private String data;

        private AtomicInteger connectCount = new AtomicInteger();

        ConnectFutureListener(String data) {
            this.data = data;
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
            } else {
                System.out.println("连接成功：触发监听操作！");
                newChannel.writeAndFlush(this.data);
            }
        }
    }
}

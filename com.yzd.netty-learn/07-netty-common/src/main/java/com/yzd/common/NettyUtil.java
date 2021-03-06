package com.yzd.common;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyUtil {


    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    static {
        if (Epoll.isAvailable()) {
            System.out.println("Using epoll");
        } else {
            System.out.println("Using java.nio");
        }
    }

    public static EventLoopGroup newBossEventLoopGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup();
        }
        return new NioEventLoopGroup();
    }

    public static EventLoopGroup newWorkerEventLoopGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(WORKER_THREADS);
        }
        return new NioEventLoopGroup(WORKER_THREADS);
    }

    public static Class<? extends ServerChannel> serverChannelType() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> clientChannelType() {
        if (Epoll.isAvailable()) {
            return EpollSocketChannel.class;
        }
        return NioSocketChannel.class;
    }
}

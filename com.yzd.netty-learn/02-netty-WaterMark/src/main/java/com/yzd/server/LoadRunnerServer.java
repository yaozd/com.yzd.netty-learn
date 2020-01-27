package com.yzd.server;

/**
 * @Author: yaozh
 * @Description:
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

/**
 * Created by lijianzhen1 on 2019/1/24.
 */
public final class LoadRunnerServer {
    private static final EventExecutorGroup EXECUTOR_GROUP = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    /**
     * Netty稳定性
     * 来源：Netty 中的流控与统计
     * https://blog.csdn.net/qq_32523587/article/details/80623295
     *
     * 流量整形处理有三个重要的参数：
     * writeLimit：每秒最多可以写多个字节的数据。
     * readLimit：每秒最多可以读多少个字节的数据。
     * checkInterval：流量检查的间隔时间，默认1s。
     * 以读操作为例，流量整形的工作过程大致如下：
     *
     *
     * 启动一个定时任务，每隔checkInterval毫秒执行一次，在任务中清除累加的读写字节数还原成0，更新上次流量整形检查时间。
     * 执行读操作，触发channelRead方法，记录当前已读取的字节数并且和上次流量整形检查之后的所有读操作读取的字节数进行累加。
     * 根据时间间隔和已读取的流量数计算当前流量判断当前读取操作是否已导致每秒读取的字节数超过了阀值readLimit，
     * 计算公式是：(bytes * 1000 / limit - interval) / 10 * 10，其中，bytes是上次流量整形检查之后的所有读操作累计读取的字节数，
     * limit 就是readLimit，interval是当前时间距上次检查经过的时间毫秒数，如果该公式计算出来的值大于固定的阀值10，那么说明流量数已经超标，那么把该读操作放到延时任务中处理，延时的毫秒数就是上面那个公式计算出来的值。
     */
    private static final GlobalTrafficShapingHandler trafficHandler = new GlobalTrafficShapingHandler(EXECUTOR_GROUP, 30, 30);
    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    TrafficCounter trafficCounter = trafficHandler.trafficCounter();
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final long totalRead = trafficCounter.cumulativeReadBytes();
                    final long totalWrite = trafficCounter.cumulativeWrittenBytes();
                    System.out.println("total read: " + (totalRead >> 10) + " KB");
                    System.out.println("total write: " + (totalWrite >> 10) + " KB");
                    System.out.println("流量监控: " + System.lineSeparator() + trafficCounter);
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(trafficHandler);
                            p.addLast(new EchoServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //ctx.write(msg);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 发生异常关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}


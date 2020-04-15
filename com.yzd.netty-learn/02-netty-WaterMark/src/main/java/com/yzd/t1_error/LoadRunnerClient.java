package com.yzd.t1_error;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK;

/**
 * @Author: yaozh
 * @Description:
 */
public class LoadRunnerClient {
    final static int M = 1024 * 1024;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

    @SuppressWarnings({"unchecked", "deprecation"})
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1 * 1024 * 1024, 5 * 1024 * 1024))
                    //设置请求的高水位 10M
                    .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 10 * 1024 * 1024)
                    //.option(WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(30 * M, 50 * M))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new LoadRunnerClientHandler());
                        }
                    });
            ChannelFuture f = b.connect(HOST, PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

 class LoadRunnerClientHandler extends ChannelInboundHandlerAdapter {
    private final ByteBuf firstMessage;
    Runnable loadRunner;

    AtomicLong sendSum = new AtomicLong(0);
    Runnable profileMonitor;

    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    /**
     * 创建客户端的handler
     */
    public LoadRunnerClientHandler() {
        firstMessage = Unpooled.buffer(SIZE);
        for (int i = 0; i < firstMessage.capacity(); i ++) {
            firstMessage.writeByte((byte) i);
        }
    }
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        loadRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ByteBuf msg = null;
                final int len = "Netty OOM Example".getBytes().length;
                while(true)
                {
                    msg = Unpooled.wrappedBuffer("Netty OOM Example".getBytes());
                    ctx.writeAndFlush(msg);
                }
            }
        };
        new Thread(loadRunner, "LoadRunner-Thread").start();
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ReferenceCountUtil.release(msg);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

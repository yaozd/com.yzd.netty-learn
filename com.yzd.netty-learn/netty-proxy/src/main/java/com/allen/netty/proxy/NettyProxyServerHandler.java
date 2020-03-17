package com.allen.netty.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * Server channel 处理函数
 *
 * @author sky_han
 */
public class NettyProxyServerHandler extends ChannelInboundHandlerAdapter {

    private String remoteHost = "127.0.0.1";
    private int remotePort = 8090;

    private Channel outBoundChannel;

    public NettyProxyServerHandler() {
        super();
    }

    public NettyProxyServerHandler(String remoteHost, int remotePort) {
        super();
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        /**
         * 重写uri
         * rewrite ^/receipt/(.*)$  => /invoice/$1 permanent;
         */
        if (msg instanceof FullHttpRequest) {
            System.out.println("this is the FullHttpRequest");
            FullHttpRequest request=(FullHttpRequest)msg;
            System.out.println("uri:"+request.getUri());
            request.setUri("/rewrite/new-uri");
        }
        if (outBoundChannel == null || !ctx.channel().isActive()) {
            /* 创建netty client,连接到远程地址 */
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("codec", new HttpClientCodec());
                            //HttpObjectAggregator
                            //HttpServerCodec是无法完全的解析Http POST请求的，因为HttpServerCodec只能获取uri中参数
                            //所以需要加上HttpObjectAggregator
                            pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                            pipeline.addLast(new NettyProxyClientHandler(ctx.channel()));
                        }
                    });
            ChannelFuture future = bootstrap.connect(remoteHost, remotePort);
            outBoundChannel = future.channel();

            /* channel建立成功后,将请求发送给远程主机 */
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        future.channel().close();
                    }
                }

            });
        } else {
            outBoundChannel.writeAndFlush(msg);
        }
    }
}

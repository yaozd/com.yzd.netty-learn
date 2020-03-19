package com.yzd.netty;

import java.util.HashMap;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Created by apple on 17/10/21.
 * netty使用(4)一个简单Http服务器
 * https://www.cnblogs.com/legion/p/8670850.html
 */
public class HttpServer {
    /**
     * http://localhost:8080/hello
     * http://localhost:8080/500
     * http://localhost:8080/302
     * http://localhost:8080/404
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        start(8080);
    }
    public static void start(final int port) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup woker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {

            serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(boss, woker)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("http-decoder",new HttpServerCodec());
                            ch.pipeline().addLast(new HttpServerHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            woker.shutdownGracefully();
        }
    }

}
class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private String content = "hello world,This is the Legion....";
    private final static String LOC = "302";
    private final static String NOT_FOND = "404";
    private final static String BAD_REQUEST = "400";
    private final static String INTERNAL_SERVER_ERROR = "500";
    private static Map<String, HttpResponseStatus> mapStatus = new HashMap<String, HttpResponseStatus>();

    static {
        mapStatus.put(LOC, HttpResponseStatus.FOUND);
        mapStatus.put(NOT_FOND, HttpResponseStatus.NOT_FOUND);
        mapStatus.put(BAD_REQUEST, HttpResponseStatus.BAD_REQUEST);
        mapStatus.put(INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            boolean keepaLive = HttpUtil.isKeepAlive(request);
            System.out.println("method" + request.method());
            System.out.println("uri" + request.uri());
            String uri = request.uri().replace("/", "").trim();
            System.out.println("uri after trim is: "+uri);
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            if (mapStatus.get(uri) != null) {
                System.out.println("yes");
                httpResponse.setStatus(mapStatus.get(uri));
                httpResponse.content().writeBytes(mapStatus.get(uri).toString().getBytes());
            } else {
                System.out.println("no");
                httpResponse.content().writeBytes(content.getBytes());
            }
            //重定向处理
            if (httpResponse.status().equals(HttpResponseStatus.FOUND)) {
                httpResponse.headers().set(HttpHeaderNames.LOCATION, "https://www.baidu.com/");
            }
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            if (keepaLive) {
                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(httpResponse);
            } else {
                ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

}
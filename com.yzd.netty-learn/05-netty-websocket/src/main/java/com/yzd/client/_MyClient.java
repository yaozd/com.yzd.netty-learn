package com.yzd.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * @Author: yaozh
 * @Description:
 */
public class _MyClient {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap boot = new Bootstrap();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean zhen = true;
        try {
            boot.option(ChannelOption.SO_KEEPALIVE, true)
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MyChannelInitializer());//引用自己的协议
            //ws协议类型
            URI websocketURI = new URI("ws://127.0.0.1:8002/websocket");
            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            //进行握手
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
            //需要协议的host和port
            Channel channel = boot.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
            WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("hookedHandler");
            handler.setHandshaker(handshaker);
            handshaker.handshake(channel);
            //阻塞等待是否握手成功
            handler.handshakeFuture().sync();
            System.out.println("成功!");
            //让线程睡眠1秒,以免数据收回慢
            Thread.sleep(1000);
            try {
                while (zhen) {
                    System.out.print("请输入操作:");
                    String zhi = br.readLine();
                    //发送textwebsocketframe格式的请求
                    //TextWebSocketFrame 可以任意转换
                    TextWebSocketFrame frame = new TextWebSocketFrame(zhi + "\r\n");
                    channel.writeAndFlush(frame);
                }
            } catch (Exception e) {
                br.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            zhen = false;
            try {
                br.close();
            } catch (Exception e2) {
                System.out.println(e2.getMessage());
            }
        } finally {
            //优雅关闭
            group.shutdownGracefully();
        }
    }

}

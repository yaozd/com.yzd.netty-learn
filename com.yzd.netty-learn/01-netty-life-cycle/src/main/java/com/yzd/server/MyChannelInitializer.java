package com.yzd.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * @Author: yaozh
 * @Description:
 */
public class MyChannelInitializer  extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //AllowHalfClosure:判断是否开启连接半关闭的功能
        //一个连接的远端关闭时本地端是否关闭
        //值为:false时(PS:默认值)，连接自动关闭。
        //值为:true时，触发 ChannelInboundHandler 的#userEventTriggered()方法，事件 ChannelInputShutdownEvent 。
        ch.config().setAllowHalfClosure(true);
        //
        System.out.println("channel isAllowHalfClosure:"+ch.config().isAllowHalfClosure());
        //
        ChannelPipeline ph = ch.pipeline();
        //处理http服务的关键handler
        ph.addLast(new HttpClientCodec());
        // Remove the following line if you don't want automatic content decompression.
        ph.addLast(new HttpContentDecompressor());
        // Uncomment the following line if you don't want to handle HttpContents.
        //ph.addLast(new HttpObjectAggregator(10*1024*1024));
        //自定义业务处理逻辑
        ph.addLast("handler", new LifeCycleTestHandler());
    }
}

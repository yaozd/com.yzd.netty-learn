package com.yzd.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;

/**
 * @Author: yaozh
 * @Description:
 */
public class MyChannelInitializer  extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        //处理http服务的关键handler
        ph.addLast(new HttpClientCodec());
        // Remove the following line if you don't want automatic content decompression.
        ph.addLast(new HttpContentDecompressor());
        // Uncomment the following line if you don't want to handle HttpContents.
        //ph.addLast(new HttpObjectAggregator(10*1024*1024));
        //自定义业务处理逻辑
        ph.addLast("handler", new LifeCycleTestHandler());
        ph.addLast("outboundHandler",new WriteListenerChannelDuplexHandler());
    }
}

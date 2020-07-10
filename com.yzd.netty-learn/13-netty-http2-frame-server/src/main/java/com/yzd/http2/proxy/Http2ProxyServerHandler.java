package com.yzd.http2.proxy;

import com.yzd.http2.client.Http2ClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.*;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;

@Slf4j
public class Http2ProxyServerHandler extends ChannelDuplexHandler {
    private List<Object> objectList = new LinkedList<>();
    private List<Object> reqList = new LinkedList<>();
    private Channel clientChannel;
    private int  state=1;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        objectList.add(msg);
        log.info("Frame-name:"+((Http2Frame) msg).name());
        if(msg instanceof Http2SettingsFrame ||msg instanceof Http2SettingsAckFrame){
            return;
        }
        if(msg instanceof Http2FrameStream){
            log.info(((Http2FrameStream) msg).id()+"");
        }
        if (state==1) {
            state=0;
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new Http2ProxyClientInitializer(ctx.channel()));
            clientChannel=bootstrap.connect("localhost", 8899).sync().channel();
            //clientChannel = future.channel();
        }
//        if (clientChannel.isActive()) {
//            reqList.add(msg);
//        }
        reqList.add(msg);

    }
    int a=1;
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().eventLoop().scheduleWithFixedDelay(()->{
            log.info("task finish time: " + System.currentTimeMillis());
            if(a==0){
                return;
            }
            a=0;
            for (Object o : reqList) {
                log.info("channel active:"+clientChannel.isActive());
                if(o instanceof Http2FrameStream){
                    log.info(((Http2FrameStream) o).id()+"");
                }
                clientChannel.writeAndFlush(o);
            }
            reqList.clear();
        },5,3,TimeUnit.SECONDS);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        for (Object o : reqList) {
            clientChannel.writeAndFlush(o);
        }
        for (Object o : objectList) {
            if (o instanceof ByteBufHolder) {
                ByteBufHolder data = ((ByteBufHolder) o);
                data.release();
                log.info("T1:DATA_refCnt:" + data.refCnt()+";channel:"+ctx.channel().id());
            }
        }
        super.handlerRemoved(ctx);
    }
}

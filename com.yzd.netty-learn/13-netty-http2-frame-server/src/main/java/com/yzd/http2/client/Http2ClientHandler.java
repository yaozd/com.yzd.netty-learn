package com.yzd.http2.client;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Frame;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class Http2ClientHandler extends ChannelDuplexHandler {
    List<Object> objectList = new LinkedList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        objectList.add(msg);
        log.info("Frame-name:"+((Http2Frame) msg).name());
        //todo 方便调试
        /*
        if(msg instanceof Http2Frame){
            String name = ((Http2Frame) msg).name();
            log.info("Frame-name:"+name);
        }
        if(msg instanceof Http2HeadersFrame){

        }
        if(msg instanceof Http2DataFrame){
            boolean endStream = ((Http2DataFrame) msg).isEndStream();
        }
        if(msg instanceof Http2StreamFrame){

        }*/
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
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

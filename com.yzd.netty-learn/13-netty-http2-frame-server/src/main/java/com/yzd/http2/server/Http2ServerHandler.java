package com.yzd.http2.server;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class Http2ServerHandler extends ChannelDuplexHandler {
    List<Object> objectList = new LinkedList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        objectList.add(msg);
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

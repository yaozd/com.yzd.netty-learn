package com.yzd.http2.proxy;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Frame;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * @author yaozh
 */
@Slf4j
public class Http2ProxyClientHandler extends ChannelDuplexHandler {
    private final Channel serverChannel;
    public Http2ProxyClientHandler(Channel serverChannel){
        this.serverChannel=serverChannel;
    }
    List<Object> objectList = new LinkedList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        objectList.add(msg);
        log.info("Frame-name:"+((Http2Frame) msg).name());
        if(msg instanceof Http2SettingsFrame||msg instanceof Http2SettingsAckFrame){
            //ctx.writeAndFlush(new )
            return;
        }
        if (serverChannel.isActive()) {
            serverChannel.writeAndFlush(msg);
        }
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

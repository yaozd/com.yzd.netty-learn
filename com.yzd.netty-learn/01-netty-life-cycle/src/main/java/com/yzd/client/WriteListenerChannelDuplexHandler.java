package com.yzd.client;

import com.yzd.event.HandlerStateEvent;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @Author: yaozh
 * @Description:
 */
public class WriteListenerChannelDuplexHandler extends ChannelDuplexHandler {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
        //自定义用户事件-fire代表传给下一个handler处理
        ctx.fireUserEventTriggered(HandlerStateEvent.FULSH_DATA_EVENT);
        //当前channel中处理
        ctx.channel().eventLoop().submit(()->{userEventTriggered(ctx,HandlerStateEvent.FULSH_DATA_EVENT);});
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) {
        System.out.println("自定义用户事件:" + obj.getClass());
    }
}

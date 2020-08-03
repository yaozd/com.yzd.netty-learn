package com.yzd.event;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;

/**
 * @Author: yaozh
 * @Description:
 */
public class HandlerStateEventHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("channel 用户事件触发-心跳检查：userEventTriggered():"+evt.getClass());
        if (evt instanceof ChannelInputShutdownEvent) {
            System.out.println("channel 用户事件触发-远程主机强制关闭连接:");
            //远程主机强制关闭连接
            System.out.println("远程主机强制关闭连接后，手动关闭当前连接");
            ctx.channel().close();
        }
        super.userEventTriggered(ctx, evt);
    }
}

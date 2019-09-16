package com.yzd.demo;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class WangInHandler2 extends ChannelInboundHandlerAdapter {
    public WangInHandler2() {
        System.out.println("WangInHandler2-init");
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        // TODO Auto-generated method stub
        //  super.channelRead(ctx, msg);
        System.out.println(" WangInHandler2 channelRead");
        //通过fireChannelRead把信息传递到下一个handler
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelReadComplete(ctx);
        System.out.println(" WangInHandler2 channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        // TODO Auto-generated method stub
        super.exceptionCaught(ctx, cause);
        System.out.println(" WangInHandler2 exceptionCaught");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelActive(ctx);
        System.out.println(" WangInHandler2 channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
        System.out.println(" WangInHandler2 channelInactive");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // TOfDO Auto-generated method stub
        super.channelRegistered(ctx);
        System.out.println(" WangInHandler2 channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelUnregistered(ctx);
        System.out.println(" WangInHandler2 channelUnregistered");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx)
            throws Exception {
        // TODO Auto-generated method stub
        super.channelWritabilityChanged(ctx);
        System.out.println(" WangInHandler2 channelWritabilityChanged");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        // TODO Auto-generated method stub
        super.userEventTriggered(ctx, evt);
        System.out.println(" WangInHandler2 userEventTriggered");
    }

}

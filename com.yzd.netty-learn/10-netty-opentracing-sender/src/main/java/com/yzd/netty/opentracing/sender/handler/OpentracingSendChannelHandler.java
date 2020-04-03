package com.yzd.netty.opentracing.sender.handler;

import com.yzd.netty.opentracing.sender.OpentracingSender;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class OpentracingSendChannelHandler  extends SimpleChannelInboundHandler {
    private OpentracingSender opentracingSender;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive");
        opentracingSender.reconnection();
        super.channelInactive(ctx);
    }
    public void attachSender(OpentracingSender opentracingSender) {
        this.opentracingSender=opentracingSender;
    }
}

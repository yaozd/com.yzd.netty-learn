package com.yzd.netty.opentracing.sender.handler;

import com.yzd.netty.opentracing.sender.OpentracingSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class OpentracingSendChannelHandler extends SimpleChannelInboundHandler {
    private static final ByteBuf HEARTBEAT_SEQUENCE =
            Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("ping", CharsetUtil.UTF_8));
    private OpentracingSender opentracingSender;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //todo 记录非200状态码的响应数据
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive");
        opentracingSender.reconnection();
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            opentracingSender.connectionAwaitLatchCountDown();
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            //如果写通道处于空闲状态,就发送心跳命令,来维持连接状态
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Occur an exception that opentracing send handler unexpected!", cause);
    }
    public void attachSender(OpentracingSender opentracingSender) {
        this.opentracingSender = opentracingSender;
    }
}

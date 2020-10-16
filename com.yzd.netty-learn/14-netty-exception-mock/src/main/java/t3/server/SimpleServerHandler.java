package t3.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Author: yaozh
 * @Description:
 */
public class SimpleServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        boolean open = ctx.channel().isOpen();
        boolean active = ctx.channel().isActive();
        boolean removed = ctx.isRemoved();
        boolean writable = ctx.channel().isWritable();
        //==============================================
        //ctx.channel().close().sync();
        for (int i = 0; i < 1_000_000; i++) {
            if (ctx.channel().isActive()) {
                ctx.channel().write(Unpooled.buffer().writeBytes("789".getBytes())).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            logger.error("B1-CHANNEL_ACTIVE", future.cause());
                        }
                    }
                });
            }
        }
        //ctx.channel().flush().close();
        //ctx.channel().flush();
        logger.error("A0-CHANNEL_ACTIVE");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {
        logger.error("A1-CHANNEL_READ");
//        while (true){
//            ctx.channel().writeAndFlush("m");
//        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("Z0-CHANNEL_INACTIVE");
//        ctx.channel().writeAndFlush(Unpooled.buffer().writeBytes("789".getBytes())).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (!future.isSuccess()) {
//                    logger.error("Error_channelFuture", future.cause());
//                }
//            }
//        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        boolean open = ctx.channel().isOpen();
        boolean active = ctx.channel().isActive();
        boolean removed = ctx.isRemoved();
        boolean writable = ctx.channel().isWritable();
        if (cause instanceof IOException && !writable) {
            ChannelOutboundBuffer channelOutboundBuffer = ctx.channel().unsafe().outboundBuffer();
            boolean remove = channelOutboundBuffer.remove();
            boolean empty = channelOutboundBuffer.isEmpty();
            //ctx.channel().unsafe().closeForcibly();
            //
//            ChannelOutboundBuffer channelOutboundBuffer = ctx.channel().unsafe().outboundBuffer();
//            if (channelOutboundBuffer != null) {
//                for (; ; ) {
//                    if (!channelOutboundBuffer.remove()) {
//                        break;
//                    }
//                }
//            }
        }
        logger.error("ERROR_EXCEPTION", cause);
        //ctx.channel().writeAndFlush("m");
    }
}
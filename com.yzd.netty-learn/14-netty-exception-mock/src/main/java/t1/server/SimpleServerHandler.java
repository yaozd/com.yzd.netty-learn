package t1.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class SimpleServerHandler extends ChannelInboundHandlerAdapter {
    private boolean isClose = false;
    private static final Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ctx.channel().close().sync();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {
        /**
         * 模拟操作：
         * 此处插入断点，然后断开客户端即可。
         */
        logger.info("IS_OPEN:[{}]RECEIVE_DATA:[{}]", ctx.channel().isOpen(), convertByteBufToString((ByteBuf) msg));
        ReferenceCountUtil.release(msg);
        for (int i = 0; i < 100; i++) {
            if (isClose) {
                logger.error("Close![{}]", ctx.channel().isOpen());
                return;
            }
            writeAndFlushData(ctx, i);
            //1.理论：通过计数的方式中间插入flush操作，可以间接性缓解
            if (i == 50) {
                //ctx.flush();
                //ctx.channel().flush();
            }
        }
        /**
         * 情况1:
         * 只write操作但不进行flush操作，则异常为：java.nio.channels.ClosedChannelException: null
         * 情况2：
         * write操作后进行flush操作，则异常为：java.io.IOException: 远程主机强迫关闭了一个现有的连接。|java.io.IOException:连接超时
         */
        //ctx.flush();
        //ctx.channel().flush();
        logger.info("END_FLUSH_DATA!");
    }

    private void writeAndFlushData(ChannelHandlerContext ctx, int i) {
        ctx.channel().write(Unpooled.buffer().writeBytes("123".getBytes())).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.error("write data:");
                if (!future.isSuccess()) {
                    isClose = true;
                    logger.error("[{}]Error", i, future.cause());
                }
            }
        });
        //ctx.channel().flush();
    }

    public String convertByteBufToString(ByteBuf buf) {
        String str;
        if (buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("inactive");
    }
}
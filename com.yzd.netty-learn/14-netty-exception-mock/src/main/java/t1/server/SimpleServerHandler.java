package t1.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import t1.client.SimpleClient;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class SimpleServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ctx.channel().close().sync();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {
        logger.info("IS_OPEN:[{}]RECEIVE_DATA:[{}]",ctx.channel().isOpen(), convertByteBufToString((ByteBuf) msg));
//        ctx.channel().writeAndFlush(Unpooled.buffer().writeBytes("123".getBytes())).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                logger.error("write data:");
//                if (!future.isSuccess()) {
//                    logger.error("Error", future.cause());
//                }
//            }
//        });
    }
    public String convertByteBufToString(ByteBuf buf) {
        String str;
        if(buf.hasArray()) { // 处理堆缓冲区
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
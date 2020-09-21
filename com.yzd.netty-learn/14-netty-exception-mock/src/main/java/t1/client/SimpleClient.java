package t1.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Author: yaozh
 * @Description:
 */
public class SimpleClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleClient.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                    }
                });
        b.connect("localhost", 8090).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    //todo 此解决方案有待验证
                    /*future.channel().eventLoop().execute(()-> {
                        logger.info("close!关闭操作与主事件不在同一个处理流程，可以避免java.nio.channels.ClosedChannelException: null异常问题！");
                        future.channel().close();
                    });*/
                    //todo 默拟：java.nio.channels.ClosedChannelException: null
                    future.channel().close();
                    future.channel().write(Unpooled.buffer().writeBytes("123".getBytes())).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                logger.error("Error", future.cause());
                            }
                        }
                    });
                    future.channel().flush();
                }else {
                    logger.error("连接失败！");
                }
            }
        });
    }
}
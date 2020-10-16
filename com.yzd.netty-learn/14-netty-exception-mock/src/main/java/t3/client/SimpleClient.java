package t3.client;

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

    private static final Logger logger = LoggerFactory.getLogger(t1.client.SimpleClient.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                    }
                });
        ChannelFuture channelFuture = b.connect("localhost", 8090).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                for (int i = 0; i < 1000_000; i++) {
                    writeData(future);
                }
                //模拟突然断开
                System.exit(3);
                if (future.isSuccess()) {
                    //todo 此解决方案有待验证

                } else {
                    logger.error("连接失败！");
                }
            }
        });
        channelFuture.channel().writeAndFlush(Unpooled.buffer().writeBytes("789".getBytes())).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                //模拟突然断开
                System.exit(3);
                if (!future.isSuccess()) {
                    logger.error("Error_channelFuture", future.cause());
                }
            }
        });
        logger.info("end");
        //System.exit(3);
    }
    private static void writeData(ChannelFuture future){
        future.channel().writeAndFlush(Unpooled.buffer().writeBytes("789".getBytes())).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.exit(3);
                if (!future.isSuccess()) {
                    logger.error("Error_channelFuture", future.cause());
                }
            }
        });
    }
}
package t2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

/**
 * @Author: yaozh
 * @Description:
 */
public class SimpleClient {
    /**
     * 场景：
     * 就是在 server 端设置一个在 channelActive 的时候就 close channel 的 handler.
     * 而在 client 端则写一个 Connect 成功后立即发送请求数据的 listener
     * 现象：
     * java.io.IOException: 你的主机中的软件中止了一个已建立的连接。
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
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
                    future.channel().write(Unpooled.buffer().writeBytes("123".getBytes()));
                    future.channel().flush();
                }else {
                    System.out.println("连接失败！");
                }
            }
        });
    }
}

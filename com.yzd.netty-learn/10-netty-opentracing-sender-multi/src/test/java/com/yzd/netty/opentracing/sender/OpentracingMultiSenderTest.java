package com.yzd.netty.opentracing.sender;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;


@Slf4j
public class OpentracingMultiSenderTest {

    @Test
    public void multiClient() {
        int clientSize = 3;
        OpentracingMultiSender sender = new OpentracingMultiSender(clientSize);
        for (int i = 0; i < clientSize - 1; i++) {
            Channel client = getClient(clientSize, sender);
            if (client != null) {
                client.writeAndFlush("send-data");
            }
        }
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            log.info(sender.channelMap.size() + "");
            for (Map.Entry<Integer, NettyClient> clientEntry :
                    sender.channelMap.entrySet()) {
                log.info("id:{};status:{}", clientEntry.getKey(), clientEntry.getValue().status);
            }
            return sender.channelMap.size() == clientSize + 1;
        });
    }

    private Channel getClient(int clientSize, OpentracingMultiSender sender) {
        for (int i = 0; i < clientSize; i++) {
            NettyClient client = sender.getClient(i);
  /*          if (NettyClient.ClientStatus.INIT.equals(client.status)) {
                //continue;
                sender.newChannel(i);
                //为了节省资源的开销，当前如果是‘初始状态’，则直接中断循环
                //
                break;
            }
            if (NettyClient.ClientStatus.FAILURE.equals(client.status)) {
                sender.newChannel(i);
                //continue;
                //为了节省资源的开销，当前如果是‘初始状态’，则直接中断循环
                //
                break;
            }*/
            if (NettyClient.ClientStatus.INIT.equals(client.status)
                    ||NettyClient.ClientStatus.FAILURE.equals(client.status)) {
                sender.newChannel(i);
                //continue;
                //为了节省资源的开销，当前如果是‘初始状态’，则直接中断循环
                //
                break;
            }
            if (NettyClient.ClientStatus.CONNECTING.equals(client.status)) {
                continue;
            }

            if (NettyClient.ClientStatus.SUCCESS.equals(client.status)) {
                Channel channel = client.getChannel();
                if (!channel.isActive()) {
                    sender.newChannel(i);
                    continue;
                }
                if (!channel.isWritable()) {
                    continue;
                }
                return channel;
            }
        }
        return null;
    }


    @Test
    public void multiChannel() {
        int clientSize = 3;
        OpentracingMultiSender sender = new OpentracingMultiSender(clientSize);
        for (int i = 0; i < clientSize; i++) {
            getChannel(clientSize, sender);
        }
        await().atMost(10, TimeUnit.SECONDS).until(() -> sender.channelMap.size() == clientSize + 1);
    }

    private Channel getChannel(int clientSize, OpentracingMultiSender sender) {
        for (int i = 0; i < clientSize; i++) {
            Channel channel = sender.getChannel(i);
            if (channel == null) {
                sender.newChannel(i);
                continue;
            }
            if (!channel.isActive()) {
                //todo connection
            }
            if (!channel.isWritable()) {
                continue;
            }
            return channel;
        }
        return null;
    }
}
package com.yzd.client;

import io.netty.channel.Channel;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @Author: yaozh
 * @Description:
 */
public class _MainTest {
    @Test
    public void Test() throws URISyntaxException, IOException {
        URI serviceDiscoverUri = new URI("http://www.baidu.com:80");
        Channel channel = NettyHttpClient.getInstance().getChannel(serviceDiscoverUri);
        channel.writeAndFlush(RequestUtil.getRequestPackage(serviceDiscoverUri));
        System.in.read();

    }
}

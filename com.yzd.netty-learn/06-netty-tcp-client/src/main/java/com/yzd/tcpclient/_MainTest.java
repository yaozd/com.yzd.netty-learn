package com.yzd.tcpclient;

import io.netty.channel.Channel;
import org.junit.AfterClass;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 通过tcp发送自定义的http get请求报文
 * 主要是模拟畸形数据包（非法数据包：不完整的http请求包）
 * @Author: yaozh
 * @Description:
 */
public class _MainTest {
    @Test
    public void sendGetRequestTest() throws URISyntaxException {
        URI uri =new URI("http://localhost:9091/sleep/1000/value/1");
        Channel channel = NettyTcpClient.getInstance().getChannel(uri);
        String content=HttpPackageUtil.buildGetRequestContent(uri,true);
        channel.writeAndFlush(content);
    }
    @AfterClass
    public static void after() throws InterruptedException {
        Thread. sleep(1000*3);
    }
}

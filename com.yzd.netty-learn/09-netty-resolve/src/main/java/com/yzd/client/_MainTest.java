package com.yzd.client;

import com.yzd.resolve.data.RequestData;
import com.yzd.resolve.data.RequestType;
import com.yzd.resolve.data.TaskInfo;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class _MainTest {
    @Test
    public void writeData_Test() throws IOException {
        //URI serviceUri = RequestUtil.newUri("http://172.20.60.45:8081/demo");
        URI serviceUri = RequestUtil.newUri("http://localhost:8090/k8s/api");
        TaskInfo taskInfo=new TaskInfo("key-1",serviceUri);
        RequestData requestData=new RequestData(taskInfo, RequestType.RAW_URI);
        NettyHttpClient.getInstance().writeData(requestData);
        System.in.read();
    }
    @Test
    public void Test() throws URISyntaxException, IOException {
        URI serviceDiscoverUri = new URI("http://www.baidu.com");
        //URI serviceDiscoverUri = new URI("http://localhost:8090/sleep/watch-uri");
        Channel channel = NettyHttpClient.getInstance().getChannel(serviceDiscoverUri);
        channel.writeAndFlush(RequestUtil.getRequestPackage(serviceDiscoverUri));
        System.in.read();

    }
}

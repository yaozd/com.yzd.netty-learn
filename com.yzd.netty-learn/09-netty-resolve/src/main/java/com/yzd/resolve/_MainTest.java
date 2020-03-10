package com.yzd.resolve;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 通过netty实现服务发现K8S-API
 */
public class _MainTest {
    @Test
    public void test() throws URISyntaxException, IOException {
        String key = "service-key";
        URI serviceDiscoverUri = new URI("http://www.baidu.com:80");
        TaskInfo taskInfo = new TaskInfo(key, serviceDiscoverUri);
        Resolver.getInstance().addTask(taskInfo);
       /* for (int i = 0; i < 100; i++) {
            Scheduler.doWork(key,taskInfo,1);
        }
        Scheduler.doWork(key,taskInfo,1);*/
        //System.in.read();
    }
}

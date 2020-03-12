package com.yzd.resolve;

import com.yzd.client.RequestUtil;
import com.yzd.resolve.data.TaskInfo;
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
        //URI serviceUri = RequestUtil.newUri("http://172.20.60.45:8081/demo");
        URI serviceUri = RequestUtil.newUri("http://localhost:8090/demo");
        TaskInfo taskInfo = new TaskInfo(key, serviceUri);
        Resolver.getInstance().addTask(taskInfo);
       /* for (int i = 0; i < 100; i++) {
            Scheduler.doWork(key,taskInfo,1);
        }
        Scheduler.doWork(key,taskInfo,1);*/
        System.in.read();
    }
}

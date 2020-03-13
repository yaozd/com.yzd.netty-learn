package com.yzd.resolve;

import com.yzd.client.RequestUtil;
import com.yzd.resolve.data.TaskInfo;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 通过netty实现服务发现K8S-API
 */
public class _MainTest {
    @Test
    public void test() throws URISyntaxException, IOException {
        String key = "service-key";
        //URI serviceUri = RequestUtil.newUri("http://172.20.60.45:8081/demo");
        URI serviceUri = RequestUtil.newUri("http://localhost:8090/k8s/api");
        TaskInfo taskInfo = new TaskInfo(key, serviceUri);
        Resolver.getInstance().addTask(taskInfo);
        /*模拟相同服务，不正定发布*/
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                int next = ThreadLocalRandom.current().nextInt(10, 100);
                System.err.println("====================sleep time :"+next+"s");
                try {
                    Thread.sleep(next*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                URI tempUri = RequestUtil.newUri("http://localhost:8090/k8s/api");
                TaskInfo tempTaskInfo = new TaskInfo(key, tempUri);
                Resolver.getInstance().addTask(tempTaskInfo);
        }
        }).start();
        System.in.read();
    }
}

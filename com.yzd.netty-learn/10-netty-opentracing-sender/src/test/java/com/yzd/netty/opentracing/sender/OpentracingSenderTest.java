package com.yzd.netty.opentracing.sender;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * @Author: yaozh
 * @Description:
 */
public class OpentracingSenderTest {

    @Test
    public void client() throws Exception {
        //String url = "http://www.baidu.com";
        String url = "http://127.0.0.1:8888";
        OpentracingSender opentracingSender=new OpentracingSender(url);
        for (int i = 0; i < 10000; i++) {
            opentracingSender.send();
        }
        Thread.sleep(1000*1000);
        await().atMost(50000, TimeUnit.SECONDS).until(()->opentracingSender.isAvailableChannel());
    }
}
package com.yzd.netty.opentracing.sender;

import jdk.nashorn.internal.AssertsEnabled;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
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
        await().atMost(50, TimeUnit.SECONDS).until(()->opentracingSender.isAvailable());
    }
}
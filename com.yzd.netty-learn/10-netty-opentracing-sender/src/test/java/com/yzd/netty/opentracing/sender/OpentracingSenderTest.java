package com.yzd.netty.opentracing.sender;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
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
        //String url = "http://127.0.0.1:8888";
        String url = "http://localhost:8090/opentracing/init";
        OpentracingSender opentracingSender=new OpentracingSender(url);
        for (int i = 0; i < 10000; i++) {
            opentracingSender.send();
        }
        Thread.sleep(10*1000);
        opentracingSender.reload("http://localhost:8090/opentracing/reload");
        Thread.sleep(1000000*1000);
        await().atMost(50000, TimeUnit.SECONDS).until(()->opentracingSender.isAvailableChannel());
    }
    @Test
    public void createUri() throws URISyntaxException {
        //URI uri=new URI("127.0.0.1:8090/PATH");
        URI uri=new URI("127.0.0.1:8090");
        //URI uri=new URI("127.0.0.1/PATH");
        if(uri.getScheme()==null){
            System.out.println("HTTP");
            uri=new URI("HTTP://"+uri.toString());
        }
        System.out.println(uri);
    }
}
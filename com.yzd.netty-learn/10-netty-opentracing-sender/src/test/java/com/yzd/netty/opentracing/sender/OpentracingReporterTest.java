package com.yzd.netty.opentracing.sender;

import org.junit.Test;

import static org.junit.Assert.*;

public class OpentracingReporterTest {

    @Test
    public void save() {
        for (int i = 0; i < 10000000; i++) {
            OpentracingReporter.getInstance().save("span");
        }
    }
    @Test
    public void report() {
        new Thread(()->{
            for (int i = 0; i < 10000000; i++) {
                if(i%777==0){
                    try {
                        Thread.sleep(5*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                OpentracingReporter.getInstance().save("span-"+i);
            }
        }).start();
        OpentracingReporter.getInstance().report();
    }
}
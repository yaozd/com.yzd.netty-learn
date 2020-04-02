package com.yzd.netty.opentracing.sender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OpentracingReporter {
    private static OpentracingReporter ourInstance = new OpentracingReporter();

    public static OpentracingReporter getInstance() {
        return ourInstance;
    }

    private OpentracingReporter() {
    }

    private BlockingQueue<String> spanDataQueue = new ArrayBlockingQueue<>(10000);
    private static final int MAX_BATCH_SEND_COUNT = 10000;
    private static final int MAX_AWAIT_SECONDS = 3;

    public void save(String span) {
        spanDataQueue.offer(span);
    }

    public void report() {
        CountDownLatch latch;
        List<String> dataList = null;
        while (true) {
            latch = new CountDownLatch(MAX_BATCH_SEND_COUNT);
            //
            dataList = new ArrayList<String>(MAX_BATCH_SEND_COUNT);
            try {
                addData(dataList, latch);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sendData(dataList);
            }
        }
    }

    private void sendData(List<String> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        try {
            System.out.println("send span data!" + dataList.get(MAX_BATCH_SEND_COUNT - 1));
        } catch (Exception ex) {
            //如果某一次发送出现异常！不影响其他
            ex.printStackTrace();
        }
    }

    private void addData(List<String> dataList, CountDownLatch latch) throws InterruptedException {
        //List<String> dataList = new ArrayList<String>((int) latch.getCount());
        while (latch.getCount() > 0) {
            String result = spanDataQueue.poll(MAX_AWAIT_SECONDS, TimeUnit.SECONDS);
            if (result == null) {
                break;
            }
            latch.countDown();
            dataList.add(result);
        }
    }
}

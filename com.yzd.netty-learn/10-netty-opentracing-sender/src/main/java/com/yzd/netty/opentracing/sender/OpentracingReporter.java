package com.yzd.netty.opentracing.sender;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yaozh
 */
public class OpentracingReporter {
    private static OpentracingReporter ourInstance = new OpentracingReporter();

    public static OpentracingReporter getInstance() {
        return ourInstance;
    }

    private OpentracingReporter() {
    }

    private BlockingQueue<String> spanDataQueue = new ArrayBlockingQueue<>(10000);
    private static final int MAX_BATCH_SEND_COUNT = 7;
    private static final int MAX_AWAIT_SECONDS = 3;

    public void save(String span) {
        spanDataQueue.offer(span);
    }

    public void report() {
        //CountDownLatch latch;
        //List<String> dataList = null;
        while (true) {
            CountDownLatch latch = new CountDownLatch(MAX_BATCH_SEND_COUNT);
            //
            List<String> dataList = Lists.newArrayListWithExpectedSize(MAX_BATCH_SEND_COUNT);
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
            //todo send data
            System.out.println(dataListToJson(dataList)+new Date());
            //System.out.println(dataListToJson(dataList) + dataList.get(MAX_BATCH_SEND_COUNT - 1));
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
    private String dataListToJson(List<String> dataList) {
        StringBuilder json = new StringBuilder();
        int size = dataList.size();
        json.append("[");
        for (int i = 0; i < size; i++) {
            json.append(dataList.get(i));
            if(i<size-1){
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
}

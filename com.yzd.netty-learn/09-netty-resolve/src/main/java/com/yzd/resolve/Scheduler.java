package com.yzd.resolve;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static EventLoopGroup work = new NioEventLoopGroup();

    /**
     * 执行调度
     *
     * @param taskInfo
     * @param millis   间隔时长
     */
    public static void doWork(TaskInfo taskInfo, long millis) {
        work.schedule(() -> {
            StringBuilder stringBuilder = new StringBuilder().append("date:").append(new Date()).append(";key:").append(taskInfo.getKey());
            System.out.println(stringBuilder.toString());
        }, millis, TimeUnit.MILLISECONDS);
    }

    public static void doWorkReadAll(TaskInfo taskInfo, long intervalTime) {
        work.schedule(() -> {
            StringBuilder stringBuilder = new StringBuilder().append("read all =").append("date:").append(new Date()).append(";key:").append(taskInfo.getKey());
            System.out.println(stringBuilder.toString());
        }, intervalTime, TimeUnit.MILLISECONDS);
    }
}

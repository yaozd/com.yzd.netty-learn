package com.yzd.netty;

import cn.hutool.core.date.DateUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class App {
    private static EventLoopGroup work = new NioEventLoopGroup();

    /**
     * Netty定时任务调度
     * <p>
     * Netty利用EventLoop实现调度任务执行
     * https://www.w3cschool.cn/essential_netty_in_action/essential_netty_in_action-pwme28eu.html
     * <p>
     * netty
     * 线程池之 newScheduledThreadPool中scheduleAtFixedRate（四个参数）
     * https://blog.csdn.net/weixin_35756522/article/details/81707276
     *
     * @param args
     */
    public static void main(String[] args) {
        printDate(1);
        work.schedule(() -> {
            printDate(2);
        }, 100, TimeUnit.MILLISECONDS);
        //initialDelay：初始化延时
        //period：两次开始执行最小间隔时间
        //work.scheduleAtFixedRate(()->{printDate(3);},5000,1000,TimeUnit.MILLISECONDS);
        //initialDelay：初始化延时
        //period：前一次执行结束到下一次执行开始的间隔时间（间隔执行延迟时间）
        //work.scheduleWithFixedDelay(()->{printDate(4);},5000,1000,TimeUnit.MILLISECONDS);
        //
        //work.shutdownGracefully();

    }

    private static void printDate(Object step) {
        System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS") + ";step:" + step);
    }
}

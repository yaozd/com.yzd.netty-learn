package com.yzd.common;

import java.util.function.Consumer;

/**
 * @Author: yaozh
 * @Description:
 */
public class MethodUtil {
    public static void methodExecuteTime(Consumer function) {
        methodExecuteTime(1, function);
    }

    public static void methodExecuteTime(int iteration, Consumer function) {
        for (int i = 0; i < iteration; i++) {
            long begin = System.currentTimeMillis(); //测试起始时间
            function.accept(null);
            long end = System.currentTimeMillis(); //测试结束时间
            System.out.println("[use time]:" + (end - begin) + "ms"); //打印使用时间
        }
    }
}

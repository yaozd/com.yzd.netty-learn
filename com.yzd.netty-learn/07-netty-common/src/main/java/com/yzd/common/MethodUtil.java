package com.yzd.common;

import java.util.function.Consumer;

/**
 * @Author: yaozh
 * @Description:
 */
public class MethodUtil {
    public static void methodExecuteTime(Consumer function) {
        long begin = System.currentTimeMillis(); //测试起始时间
        function.accept(null);
        long end = System.currentTimeMillis(); //测试结束时间
        System.out.println("[use time]:" + (end - begin) + "ms"); //打印使用时间
    }
}

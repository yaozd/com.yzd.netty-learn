package com.yzd.common;

import java.util.Arrays;

/**
 * @Author: yaozh
 * @Description:
 */

public class DataUtil {
    private DataUtil() {
    }

    /**
     * 获得模拟的请求数据包
     * 4MB=1024*1024*4
     *
     * @param size
     * @return
     */
    public static String getMockData(int size) {
        byte[] b = new byte[size];
        //二进制0
        Arrays.fill(b, (byte) 0);
        return new String(b);
    }
}

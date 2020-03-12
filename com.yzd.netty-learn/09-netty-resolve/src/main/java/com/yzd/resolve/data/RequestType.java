package com.yzd.resolve.data;

/**
 * @Author: yaozh
 * @Description:
 */
public enum RequestType {
    /**
     * 原始URI
     */
    RAW_URI(1),
    READ_ALL_URI(2),
    WATCH_URI(3);

    private final int value;

    RequestType(int i) {
        this.value=i;
    }
    public int getValue(){
        return value;
    }
}

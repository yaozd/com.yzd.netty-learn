package com.yzd.resolve;

/**
 * @Author: yaozh
 * @Description:
 */
public enum RequestTypeEnum {
    READ_ALL_URI(1),
    WATCH_URI(2);

    private final int value;

    RequestTypeEnum(int i) {
        this.value=i;
    }
    public int getValue(){
        return value;
    }
}

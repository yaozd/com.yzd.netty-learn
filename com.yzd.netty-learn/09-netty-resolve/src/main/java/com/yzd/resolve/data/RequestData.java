package com.yzd.resolve.data;

import lombok.Data;

/**
 * @Author: yaozh
 * @Description:
 */
@Data
public class RequestData {
    public RequestData(TaskInfo taskInfo, RequestType requestType) {
        this.taskInfo = taskInfo;
        this.requestType = requestType;
    }

    private RequestType requestType;

    private TaskInfo taskInfo;
    /**
     * 执行间隔时间，主要用于在请求地址不可用时，防止频繁发送请求使用
     */
    private long intervalTime=0L;
    /**
     * 请求失败计数器，用于调整执行间隔时间
     */
    private int failCount=0;
    /**
     * 请求的唯一ID，解决响应延迟，产生的数据不一致
     * PS：第二请求先于第一个请求响应
     */
    private String uuid;

    public void incrementConnectionFail() {
        failCount++;
    }

    public void resetConnectionFail() {
        failCount=0;
    }
}

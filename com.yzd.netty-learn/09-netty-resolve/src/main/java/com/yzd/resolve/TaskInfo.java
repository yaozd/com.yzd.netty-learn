package com.yzd.resolve;

import java.net.URI;
import java.util.UUID;

public class TaskInfo {
    private String key;
    /**
     * uuid 同一个KEY，发布多次服务发现地址
     */
    private String uuid;
    private URI uri;
    private URI readAllUri;
    private URI watchUri;
    /**
     * 执行间隔时间，主要用于在请求地址不可用时，防止频繁发送请求使用
     */
    private long intervalTime=0L;
    /**
     * 请求失败计数器，用于调整执行间隔时间
     */
    private int failCount=0;

    public TaskInfo(String key, URI uri) {
        this.key = key;
        this.uri = uri;
        this.readAllUri = uri;
        this.watchUri = uri;
        this.uuid = UUID.randomUUID().toString();
    }

    public URI getReadAllUri() {
        return readAllUri;
    }

    public URI getWatchUri() {
        return watchUri;
    }

    public String getKey() {
        return key;
    }

    public String getUuid() {
        return uuid;
    }

    public long getIntervalTime() {
        return intervalTime;
    }
}

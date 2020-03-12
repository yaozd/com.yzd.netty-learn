package com.yzd.resolve.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * @author yaozh
 */
public class TaskInfo {
    private String key;
    /**
     * uuid 同一个KEY，发布多次服务发现地址
     */
    private String uuid;
    private URI uri;
    /**
     * 拉取全部节点信息URI
     */
    private URI readAllUri;
    /**
     * 监听节点URI
     */
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
        this.readAllUri = toReadAllUri(uri);
        this.watchUri = toWatchUri(uri);
        this.uuid = UUID.randomUUID().toString();
    }

    private URI toWatchUri(URI uri) {
        StringBuilder sb=new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getAuthority())
                .append("/sleep/watch-uri");
        return newUri(sb.toString());
    }

    private URI toReadAllUri(URI uri) {
        StringBuilder sb=new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getAuthority())
                .append("/sleep/read-all-uri");
        return newUri(sb.toString());
    }

    private URI newUri(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("create uri exception!",e);
        }
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

    public URI getUri() {
        return uri;
    }
}

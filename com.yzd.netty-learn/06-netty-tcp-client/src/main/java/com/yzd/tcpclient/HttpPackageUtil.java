package com.yzd.tcpclient;

import java.net.URI;

/**
 * @Author: yaozh
 * @Description:
 */
public class HttpPackageUtil {
    /**
     * 生成HTTP GET请求的数据包
     * @param uri
     * @param isFullData
     * @return
     */
    public static String buildGetRequestContent(URI uri, boolean isFullData) {
        StringBuilder content = new StringBuilder();
        content.append("GET ").append(uri.toString()).append(" HTTP/1.1\r\n");
        content.append("Host:").append(uri.getHost()).append(" \r\n");
        content.append("Connection: Keep-Alive\r\n");
        /**
         * 使用场景：超过最大请求头（API-ROUTER的默认值为8K）时
         * content.append("cookie:"+ PackageUtil.size1MB()+"\r\n");*/
        //注，这是关键的关键，"\r\n"代表的lastHttpContent,如果没有就不是一个完整的头信息。这里一定要一个回车换行，表示消息头完，不然服务器会等待
        if (isFullData) {
            content.append("\r\n");
        }
        return content.toString();
    }
}

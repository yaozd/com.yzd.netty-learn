package com.yzd.client;

import com.yzd.resolve.data.RequestData;
import com.yzd.resolve.data.RequestType;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author: yaozhendong
 * @create: 2019-11-25 14:43
 **/

public class RequestUtil {
    private RequestUtil() {
    }

    public static DefaultFullHttpRequest getRequestPackage(URI uri) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
        // 构建http请求
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        return request;
    }

    /**
     * @param uri
     * @param content 设置POST数据包中传输的数据
     * @return
     */
    public static DefaultFullHttpRequest getRequestPackageForPostMethod(URI uri, String content) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath()
                , Unpooled.wrappedBuffer(getBytes(content, "UTF-8")));
        // 构建http请求
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        /**
         * //JSON格式的请求头
         * //'Content-Type: application/json'
         * request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
         */
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        return request;
    }

    private static byte[] getBytes(String content, String charsetName) {
        try {
            return content.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
    public static URI getUri(RequestData requestData){
        if(RequestType.RAW_URI.equals( requestData.getRequestType())){
            return requestData.getTaskInfo().getUri();
        }
        if(RequestType.READ_ALL_URI.equals( requestData.getRequestType())){
            return requestData.getTaskInfo().getReadAllUri();
        }
        if(RequestType.WATCH_URI.equals(requestData.getRequestType())){
            return requestData.getTaskInfo().getWatchUri();
        }
        throw new IllegalArgumentException("not found type;type=" + requestData.getRequestType());
    }
    public static URI newUri(String str) {
        try {
            URI uri= new URI(str);
            if(uri.getHost()==null){
                throw new URISyntaxException(uri.toString(),"uri.getHost()==null");
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(),e);
        }
    }
}

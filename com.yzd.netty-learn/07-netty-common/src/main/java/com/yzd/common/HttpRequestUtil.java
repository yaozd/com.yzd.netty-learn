package com.yzd.common;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

public class HttpRequestUtil {

    public static DefaultFullHttpRequest createFullHttpRequest(URI uri, HttpVersion httpVersion, HttpMethod httpMethod, Map<String, String> headers) {
        return createFullHttpRequest(uri, httpVersion, httpMethod, headers, null, null);
    }

    public static DefaultFullHttpRequest createFullHttpRequest(URI uri, HttpVersion httpVersion, HttpMethod httpMethod, Map<String, String> headers, String body, String bodyEncoding) {
        DefaultFullHttpRequest request;
        if (StringUtil.isNullOrEmpty(body)) {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath());
        } else {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath()
                    , Unpooled.wrappedBuffer(getBytes(body, bodyEncoding)));
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        }
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.headers().set(header.getKey(), header.getValue());
            }
        }
        return request;

    }

    private static byte[] getBytes(String content, String charsetName) {
        try {
            return content.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}

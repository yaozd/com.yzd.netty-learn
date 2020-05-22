package com.yzd.netty.resolver.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

/**
 * @author yaozh
 */
public class HttpRequestUtil {
    private HttpRequestUtil() {

    }

    public static DefaultFullHttpRequest createFullHttpRequest(URI uri, HttpVersion httpVersion, HttpMethod httpMethod,  Map<CharSequence, Object> headers) {
        return createFullHttpRequest(uri, httpVersion, httpMethod, headers, null, null);
    }

    public static DefaultFullHttpRequest createFullHttpRequest(URI uri, HttpVersion httpVersion, HttpMethod httpMethod,  Map<CharSequence, Object> headers, String body, String bodyEncoding) {
        DefaultFullHttpRequest request;
        if (StringUtil.isNullOrEmpty(body)) {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath());
        } else {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath()
                    , Unpooled.wrappedBuffer(getBytes(body, bodyEncoding)));
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        }
        addHeader(uri, headers, request);
        return request;

    }

    public static DefaultFullHttpRequest createFullHttpRequest(URI uri, HttpVersion httpVersion, HttpMethod httpMethod,  Map<CharSequence, Object> headers, ByteBuf body) {
        DefaultFullHttpRequest request;
        if (body == null) {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath());
        } else {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath(), body);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        }
        addHeader(uri, headers, request);
        return request;

    }

    private static void addHeader(URI uri, Map<CharSequence, Object> headers, DefaultFullHttpRequest request) {
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        if (headers != null) {
            for (Map.Entry<CharSequence, Object> header : headers.entrySet()) {
                request.headers().set(header.getKey(), header.getValue());
            }
        }
    }

    private static byte[] getBytes(String content, String charsetName) {
        try {
            return content.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}

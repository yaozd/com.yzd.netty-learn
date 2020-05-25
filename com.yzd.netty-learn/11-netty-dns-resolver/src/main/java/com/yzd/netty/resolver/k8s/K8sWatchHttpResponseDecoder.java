package com.yzd.netty.resolver.k8s;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.List;

/**
 * @Author: yaozh
 * @Description:
 */
public class K8sWatchHttpResponseDecoder extends HttpResponseDecoder {
    private static final int CARRIAGE_RETURN = 13;
    private static final int LINE_FEED = 10;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        boolean fullChunkData = isFullChunkData(buffer);
        super.decode(ctx, buffer, out);
        if (out.size() == 1 && !(out.get(0) instanceof HttpContent)) {
            return;
        }
        if (fullChunkData) {
            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
        }
    }

    /**
     * chunk-data CRLF
     * https://tools.ietf.org/html/rfc7230#section-4.1
     *
     * @param buffer
     * @return
     */
    private boolean isFullChunkData(ByteBuf buffer) {
        byte[] bytes = ByteBufUtil.getBytes(buffer);
        if (bytes == null || bytes.length < 3) {
            return false;
        }
        int cr = bytes[bytes.length - 2];
        int lf = bytes[bytes.length - 1];
        if (cr == CARRIAGE_RETURN && lf == LINE_FEED) {
            return true;
        }
        return false;
    }
}

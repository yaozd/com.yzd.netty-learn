package com.yzd.http.server.tester.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.DecoderResultProvider;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler {
    public static final String HEADER_NAME_UUID = "uuid";
    private HttpRequest request;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof DecoderResultProvider) {
            DecoderResult decoderResult = ((DecoderResultProvider) msg).decoderResult();
            if (decoderResult.isFailure()) {
                log.warn("Decode failed! remote address:{}, message:{}",
                        ctx.channel().remoteAddress(), decoderResult.toString());
                return;
            }
        }
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
        }
        /**
         *
         */
        if (msg instanceof LastHttpContent) {
            HttpResponseStatus responseStatus=HttpResponseStatus.OK;
            String uuid = request.headers().get(HEADER_NAME_UUID);
            if(StringUtils.isEmpty(uuid)){
                responseStatus=HttpResponseStatus.FORBIDDEN;
                uuid="not found uuid";
                log.warn(uuid);
            }
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, responseStatus);
            response.headers().set(HEADER_NAME_UUID, uuid);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);;
        }
    }
}

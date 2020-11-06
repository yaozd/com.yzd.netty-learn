package com.yzd.http.server.tester.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.DecoderResultProvider;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.netty.buffer.Unpooled.copiedBuffer;
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
        System.out.println(ctx.channel().id());
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
                //responseStatus=HttpResponseStatus.NOT_MODIFIED;
                //responseStatus=HttpResponseStatus.NO_CONTENT;
               // responseStatus=HttpResponseStatus.RESET_CONTENT;
                //responseStatus=HttpResponseStatus.PARTIAL_CONTENT;
               // responseStatus=HttpResponseStatus.MOVED_PERMANENTLY;
                uuid="not found uuid";
                log.warn(uuid);
            }
            ByteBuf content = copiedBuffer("data", CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, responseStatus,content);
            response.headers().set(HEADER_NAME_UUID, uuid);
            response.headers().set("Connection", "close");
            //response.headers().set("Connection", "keep-alive");
            response.headers().set("Transfer-Encoding", "chunked");
            //response.headers().set("Content-Length", "0");
            //response.headers().set("Content-Length", "0");
            //ctx.writeAndFlush(response);
            //ctx.writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT);
            //ctx.writeAndFlush(responseOK(responseStatus,content));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
    private FullHttpResponse responseOK(HttpResponseStatus status, ByteBuf content) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set("Content-Type", "text/plain;charset=UTF-8");
            //response.headers().set("Transfer-Encoding", "chunk");
            //response.headers().set("Content-Length", "100");
            //PS:Content-Length的长度必须与body的长度一致或小于才可以。（小于时则解析body不完整）
            //response.headers().set("Content-Length", response.content().readableBytes());
        }
        return response;
    }
}

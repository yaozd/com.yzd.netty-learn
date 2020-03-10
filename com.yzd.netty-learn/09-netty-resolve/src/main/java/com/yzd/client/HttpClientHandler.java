package com.yzd.client;

import com.yzd.resolve.RequestTypeEnum;
import com.yzd.resolve.TaskInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @Author: yaozh
 * @Description:
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private RequestTypeEnum requestType;
    private TaskInfo taskInfo;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if(msg instanceof  FullHttpResponse){
            System.out.println("this is the FullHttpResponse");
        }
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            System.err.println("STATUS: " + response.getStatus());
            System.err.println("VERSION: " + response.getProtocolVersion());
            System.err.println();

            if (!response.headers().isEmpty()) {
                for (String name: response.headers().names()) {
                    for (String value: response.headers().getAll(name)) {
                        System.err.println("HEADER: " + name + " = " + value);
                    }
                }
                System.err.println();
            }

            if (HttpHeaders.isTransferEncodingChunked(response)) {
                System.err.println("CHUNKED CONTENT {");
            } else {
                System.err.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            System.err.print(content.content().toString(CharsetUtil.UTF_8));
            System.err.flush();

            if (content instanceof LastHttpContent) {
                System.err.println("} END OF CONTENT");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void attachStream(TaskInfo taskInfo, RequestTypeEnum requestType) {
        this.taskInfo=taskInfo;
        this.requestType=requestType;
    }
}
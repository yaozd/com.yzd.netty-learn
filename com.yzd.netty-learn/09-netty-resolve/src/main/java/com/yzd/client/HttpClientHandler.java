package com.yzd.client;

import com.yzd.resolve.Resolver;
import com.yzd.resolve.data.RequestData;
import com.yzd.resolve.data.RequestType;
import com.yzd.resolve.data.TaskInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.Date;

/**
 * @Author: yaozh
 * @Description:
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    /**
     * 客户端请求的心跳命令
     */
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("ping", CharsetUtil.UTF_8));
    /**
     * 循环次数
     */
    private int idleCounter = 0;
    private RequestType requestType;
    private TaskInfo taskInfo;
    private RequestData requestData;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        resetIdleCounter();
        if (msg instanceof FullHttpResponse) {
            System.out.println("this is the FullHttpResponse");
        }
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            System.err.println("STATUS: " + response.getStatus());
            System.err.println("VERSION: " + response.getProtocolVersion());
            System.err.println();

            if (!response.headers().isEmpty()) {
                for (String name : response.headers().names()) {
                    for (String value : response.headers().getAll(name)) {
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
                //ctx.close();
            }
        }
        if (RequestType.WATCH_URI.equals(requestData.getRequestType())) {
            Resolver.getInstance().addRequestDataQueue(new RequestData(requestData.getTaskInfo(), RequestType.READ_ALL_URI));
            if (ctx.channel().isActive()&&Resolver.getInstance().isExistTaskInfo(requestData.getTaskInfo())) {
                URI uri = RequestUtil.getUri(requestData);
                ctx.channel().writeAndFlush(RequestUtil.getRequestPackage(uri));
            }
            return;
        }
        if (RequestType.READ_ALL_URI.equals(requestData.getRequestType())) {
            //TODO addNode();
        }
        ctx.close();
    }

    private void resetIdleCounter() {
        idleCounter = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 被关闭：channelInactive()");
        if (RequestType.WATCH_URI.equals(requestData.getRequestType())) {
            Resolver.getInstance().addRequestDataQueue(requestData);
        }
        super.channelInactive(ctx);
    }

    /**
     * 心跳请求处理
     * 每4秒发送一次心跳请求;
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        System.out.println("循环请求的时间：" + new Date() + "，次数" + idleCounter);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            //如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                idleCounter++;
            }
        }
    }

    public void attachStream(RequestData requestData) {
        this.requestData = requestData;
    }
}
package com.yzd.netty.resolver.k8s;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverQueryChannelHandler extends K8sResolverChannelHandler {


    public K8sResolverQueryChannelHandler(K8sResolverProvider resolverProvider, RequestType requestType, URI uri) {
        super(resolverProvider, requestType, uri);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            int statusCode = httpResponse.status().code();
            log.debug("RESPONSE STATUS:" + statusCode);
            if (statusCode != SUCCESS_CODE) {
                log.warn("Response fail!,code{},resolver info({}).", statusCode, getResolverInfo());
                resolverProvider.parseSuccess = false;
                ctx.close();
                return;
            }
            log.debug("CONTENT_TYPE:" + httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE));
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            String content = httpContent.content().toString(UTF_8);
            log.info("HTTP_CONTENT:" + content);
            reloadNode(content);
        }
        if (msg instanceof LastHttpContent) {
            log.debug("LastHttpContent");
        }
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) {
        super.userEventTriggered(ctx, obj);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) {
                log.error("K8s resolver fail! read timeout!,resolver info({}).", getResolverInfo());
                ctx.close();
            }
        }
    }
}

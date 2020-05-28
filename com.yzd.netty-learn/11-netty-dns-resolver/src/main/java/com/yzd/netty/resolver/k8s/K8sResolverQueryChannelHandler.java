package com.yzd.netty.resolver.k8s;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Collections;

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
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse httpResponse = (FullHttpResponse) msg;
            int statusCode = httpResponse.status().code();
            log.debug("RESPONSE STATUS:" + statusCode);
            log.debug("CONTENT_TYPE:" + httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE));
            String content = httpResponse.content().toString(UTF_8);
            log.info("HTTP_CONTENT:" + content);
            if (statusCode != SUCCESS_CODE) {
                if (statusCode == NOT_FOUND_CODE) {
                    //clear node data
                    resolverProvider.reloadNode(Collections.emptySet());
                }
                StringBuilder errorMsg = new StringBuilder()
                        .append("response code:").append(statusCode)
                        .append(",content:").append(content);
                log.error("K8s resolver fail! resolver info({}),{}.", getResolverInfo(), errorMsg);
                resolverProvider.parseSuccess = false;
                ctx.close();
                return;
            }
            reloadNode(content);
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
                resolverProvider.parseSuccess = false;
                ctx.close();
            }
        }
    }
}

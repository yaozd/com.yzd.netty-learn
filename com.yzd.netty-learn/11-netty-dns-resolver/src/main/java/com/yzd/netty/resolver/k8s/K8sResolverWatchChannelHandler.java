package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.yzd.netty.resolver.k8s.K8sResolverProvider.WRITER_IDLE_TIME_SECOND;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverWatchChannelHandler extends K8sResolverChannelHandler {
    /**
     * 聚合超时时间
     */
    private static final int AGGREGATOR_TIMEOUT_SECONDS = 10;
    private List<String> contentList = new ArrayList<>();
    private int writeIdleCount = 0;

    public K8sResolverWatchChannelHandler(K8sResolverProvider resolverProvider, RequestType requestType, URI uri) {
        super(resolverProvider, requestType, uri);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            int statusCode = httpResponse.status().code();
            log.debug("RESPONSE STATUS:" + statusCode);
            if (statusCode != SUCCESS_CODE) {
                log.warn("K8s resolver fail! response code{},resolver info({}).", statusCode, getResolverInfo());
                resolverProvider.parseSuccess = false;
                ctx.close();
                return;
            }
            log.debug("CONTENT_TYPE:" + httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE));
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf buf = httpContent.content();
            String content = buf.toString(io.netty.util.CharsetUtil.UTF_8);
            log.debug("HTTP_CONTENT:" + buf.toString(io.netty.util.CharsetUtil.UTF_8));
            if (StringUtils.isNotBlank(content)) {
                contentList.add(content);
            }
        }
        if (msg instanceof LastHttpContent) {
            log.debug("LastHttpContent");
            writeIdleCount = 0;
            String fullContent = StringUtils.join(contentList.toArray());
            contentList.clear();
            log.info("HTTP_FULL_CONTENT:" + fullContent);
            Map<String, Object> objectMap = JsonUtils.toMap(fullContent);
            Object object = objectMap.get("object");
            if (object == null) {
                log.warn("K8s resolver ! no valid object,resolver info({}).", getResolverInfo());
                resolverProvider.parseSuccess = false;
                ctx.close();
                return;
            }
            String objectContent = JsonUtils.toJSONString(object);
            log.info("Object string value:" + objectContent);
            reloadNode(objectContent);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) {
        super.userEventTriggered(ctx, obj);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                aggregationTimeout(ctx);
            }
        }
    }

    /**
     * 聚合超时操作
     *
     * @param ctx
     */
    private void aggregationTimeout(ChannelHandlerContext ctx) {
        if (contentList.size() == 0) {
            return;
        }
        if (writeIdleCount * WRITER_IDLE_TIME_SECOND > AGGREGATOR_TIMEOUT_SECONDS) {
            log.error("K8s resolver ! aggregator timeout,exceed max {} seconds,resolver info({}).", AGGREGATOR_TIMEOUT_SECONDS, getResolverInfo());
            resolverProvider.parseSuccess = false;
            ctx.close();
            return;
        }
        writeIdleCount++;
    }

}

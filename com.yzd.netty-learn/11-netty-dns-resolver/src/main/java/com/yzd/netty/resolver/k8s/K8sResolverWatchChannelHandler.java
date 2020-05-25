package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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

import static com.yzd.netty.resolver.k8s.RequestType.QUERY;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverWatchChannelHandler extends SimpleChannelInboundHandler {
    //聚合超时
    private static final int AGGREGATOR_TIMEOUT_SECONDS = 10;
    private static final int SUCCESS_CODE = 200;
    private final K8sResolverProvider resolverProvider;
    private final RequestType requestType;
    private final URI uri;
    private long resourceVersion = 0L;
    private List<String> contentList = new ArrayList<>();


    public K8sResolverWatchChannelHandler(K8sResolverProvider resolverProvider, RequestType requestType, URI uri) {
        this.resolverProvider = resolverProvider;
        this.requestType = requestType;
        this.uri = uri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            int statusCode = httpResponse.status().code();
            log.debug("RESPONSE STATUS:" + statusCode);
            if (statusCode != SUCCESS_CODE) {
                log.info("RESPONSE STATUS:" + statusCode);
                resolverProvider.parseSuccess = false;
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
            String fullContent=StringUtils.join(contentList.toArray());
            log.info("HTTP_FULL_CONTENT:" + fullContent);
            Map<String, Object> objectMap = JsonUtils.toMap(fullContent);
            Object object=objectMap.get("object");
            if(object==null){
                log.warn("No valid object,,resolver info({}).", getResolverInfo());
                resolverProvider.parseSuccess = false;
                ctx.close();
                return;
            }
            log.info("Object string value:"+String.valueOf(object));
            contentList.clear();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!resolverProvider.parseSuccess) {
            resolverProvider.reconnection();
            return;
        }
        if (QUERY.equals(requestType)) {
            resolverProvider.watch();
            return;
        }
        resolverProvider.reconnection();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isWritable()) {
            ctx.flush().close();
            return;
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                /**log.info("when WRITER_IDLE,version:{}", version);*/
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Occur an exception that opentracing send handler unexpected!", cause);
    }

    private String getResolverInfo() {
        StringBuilder nodeInfo = new StringBuilder();
        nodeInfo.append("request type:").append(requestType)
                .append(",uri:").append(uri);
        return nodeInfo.toString();
    }
}

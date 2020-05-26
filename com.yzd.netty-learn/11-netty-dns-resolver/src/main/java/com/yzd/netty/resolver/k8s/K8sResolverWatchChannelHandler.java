package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.utils.JsonUtils;
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
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverWatchChannelHandler extends K8sResolverChannelHandler {
    /**
     * 聚合超时时间
     */
    private static final int CHUNK_DATA_AGGREGATOR_TIMEOUT_SECONDS = 10;
    /**
     * 长连接超时时间
     * 默认情况下：K8S的watch api有效长连接为1小时，当到达1小时后K8S会发送last chunk data ，
     * 此时客户端需要关闭当前连接。但如果使用长连接超时时间小于1小时相当于忽略了last chunk data 相关的处理逻辑。
     */
    private static final int KEEPALIVE_TIMEOUT_SECONDS = 60 * 60;
    private int keepaliveCount = 0;
    private List<String> contentList = new ArrayList<>();
    private int writeIdleCount = 0;
    private static final String WATCH_JSON_OBJECT = "object";


    public K8sResolverWatchChannelHandler(K8sResolverProvider resolverProvider, RequestType requestType, URI uri) {
        super(resolverProvider, requestType, uri);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (!ctx.channel().isActive()) {
            return;
        }
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
            String content = httpContent.content().toString(UTF_8);
            log.debug("HTTP_CONTENT:" + content);
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
            //Chunk模式应答以0长度的块（ "0\r\n\r\n".）结束
            //默认情况下：K8S的watch api有效长连接为1小时，当到达1小时后K8S会发送last chunk data ，此时客户端需要关闭当前连接。
            if (StringUtils.isBlank(fullContent)) {
                log.info("K8s resolver! last chunk data,full content is blank,close channel,resolver info({}).", getResolverInfo());
                ctx.close();
                return;
            }
            Map<String, Object> objectMap = JsonUtils.toMap(fullContent);
            if (objectMap == null || objectMap.get(WATCH_JSON_OBJECT) == null) {
                log.error("K8s resolver fail! resolver info({}),no valid object,close channel,full content:{}.", getResolverInfo(), fullContent);
                resolverProvider.parseSuccess = false;
                ctx.close();
                return;
            }
            Object object = objectMap.get(WATCH_JSON_OBJECT);
            String objectContent = JsonUtils.toJSONString(object);
            log.info("Object_string_value:" + objectContent);
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
                //keepaliveTimeout(ctx);
            }
        }
    }

    /**
     * Chunk data 聚合超时操作
     *
     * @param ctx
     */
    private void aggregationTimeout(ChannelHandlerContext ctx) {
        if (contentList.isEmpty()) {
            return;
        }
        if (writeIdleCount * WRITER_IDLE_TIME_SECOND > CHUNK_DATA_AGGREGATOR_TIMEOUT_SECONDS) {
            log.error("K8s resolver ! aggregator timeout,exceed max {} seconds,resolver info({}).", CHUNK_DATA_AGGREGATOR_TIMEOUT_SECONDS, getResolverInfo());
            resolverProvider.parseSuccess = false;
            ctx.close();
            return;
        }
        writeIdleCount++;
    }

    /**
     * 长连接超时操作
     *
     * @param ctx
     */
    private void keepaliveTimeout(ChannelHandlerContext ctx) {
        if (keepaliveCount * WRITER_IDLE_TIME_SECOND > KEEPALIVE_TIMEOUT_SECONDS) {
            log.info("K8s resolver ! keepalive timeout,exceed max {} seconds,resolver info({}).", KEEPALIVE_TIMEOUT_SECONDS, getResolverInfo());
            ctx.close();
            return;
        }
        keepaliveCount++;
    }

}

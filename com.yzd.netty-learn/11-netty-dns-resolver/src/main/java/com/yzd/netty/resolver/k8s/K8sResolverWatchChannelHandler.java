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

import static com.yzd.netty.resolver.k8s.K8sResolverProvider.READER_IDLE_TIME;
import static com.yzd.netty.resolver.k8s.K8sResolverProvider.WRITER_IDLE_TIME;
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
    private static final int CHUNK_DATA_AGGREGATOR_TIMEOUT = 10 * 1000;
    /**
     * 长连接超时时间
     * 默认情况下：K8S的watch api有效长连接为1小时，当到达1小时后K8S会发送last chunk data ，
     * 此时客户端需要关闭当前连接。但如果使用长连接超时时间小于1小时相当于忽略了last chunk data 相关的处理逻辑。
     * <p>
     * 最大监听时长，暂定设置为1分钟
     */
    private static final int KEEPALIVE_TIMEOUT = 60 * 1000;
    private static final String WATCH_JSON_OBJECT = "object";
    private static final String WATCH_JSON_TYPE = "type";
    private int keepaliveCount = 0;
    private List<String> contentList = new ArrayList<>();
    private int writeIdleCount = 0;


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
            //默认情况下：K8S的watch api有效长连接为1小时，当到达1小时后K8S会发送last chunk data ，
            //此时客户端需要关闭当前连接。
            if (StringUtils.isBlank(fullContent)) {
                log.info("K8s resolver! last chunk data,full content is blank,close channel,resolver info({})."
                        , getResolverInfo());
                ctx.close();
                return;
            }
            Map<String, Object> objectMap = JsonUtils.toMap(fullContent);
            //检查失败
            if (!checkResponseObject(ctx, fullContent, objectMap)) {
                return;
            }
            Object object = objectMap.get(WATCH_JSON_OBJECT);
            String objectContent = JsonUtils.toJSONString(object);
            log.info("Object_string_value:" + objectContent);
            reloadNode(objectContent);
        }
        keepaliveCount = 0;
    }

    /**
     * 检查watch操作的响应数据
     * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/
     * GET /api/v1/watch/namespaces/
     * -------------------------------------------------------------------
     * WatchEvent
     * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/#watchevent-v1-meta
     * Object is:
     * * If Type is Added or Modified: the new state of the object.
     * * If Type is Deleted: the state of the object immediately before deletion.
     * * If Type is Error: *Status is recommended; other types may make sense depending on context.
     *
     * @param ctx
     * @param fullContent
     * @param objectMap
     * @return
     */
    private boolean checkResponseObject(ChannelHandlerContext ctx, String fullContent, Map<String, Object> objectMap) {
        if (objectMap == null || objectMap.get(WATCH_JSON_OBJECT) == null) {
            log.error("K8s resolver fail! resolver info({}),close the channel when no valid object,full content:{}."
                    , getResolverInfo(), fullContent);
            resolverProvider.parseSuccess = false;
            ctx.close();
            return false;
        }
        String eventStr = String.valueOf(objectMap.get(WATCH_JSON_TYPE));
        EventType event = EventType.getByType(eventStr);
        if (EventType.ADDED.equals(event) || EventType.MODIFIED.equals(event)) {
            return true;
        }
        if (EventType.DELETED.equals(event)) {
            log.warn("K8s resolver! resolver info({}),close the channel when got DELETED event,full content:{}."
                    , getResolverInfo(), fullContent);
            ctx.close();
            return false;
        }
        if (EventType.ERROR.equals(event)) {
            log.error("K8s resolver fail! resolver info({}),close the channel when got ERROR event,full content:{}."
                    , getResolverInfo(), fullContent);
            resolverProvider.parseSuccess = false;
            ctx.close();
            return false;
        }
        log.error("K8s resolver fail! resolver info({}),close the channel when unrecognized event,full content:{}."
                , getResolverInfo(), fullContent);
        resolverProvider.parseSuccess = false;
        ctx.close();
        return false;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) {
        super.userEventTriggered(ctx, obj);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                aggregationTimeout(ctx);
            }
            if (IdleState.READER_IDLE.equals(event.state())) {
                keepaliveTimeout(ctx);
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
        if (writeIdleCount * WRITER_IDLE_TIME > CHUNK_DATA_AGGREGATOR_TIMEOUT) {
            log.error("K8s resolver ! aggregator timeout," +
                            "close the channel after exceed max aggregation time {} milliseconds,resolver info({})."
                    , CHUNK_DATA_AGGREGATOR_TIMEOUT, getResolverInfo());
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
        keepaliveCount++;
        if (keepaliveCount * READER_IDLE_TIME >= KEEPALIVE_TIMEOUT) {
            log.info("K8s resolver ! keepalive timeout," +
                            "close the channel after exceed max listening time {} milliseconds ,resolver info({})."
                    , KEEPALIVE_TIMEOUT, getResolverInfo());
            ctx.close();
            return;
        }
    }

}

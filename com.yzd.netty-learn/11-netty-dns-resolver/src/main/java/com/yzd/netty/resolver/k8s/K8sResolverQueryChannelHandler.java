package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.k8s.entity.K8sServiceAddress;
import com.yzd.netty.resolver.k8s.entity.K8sServiceInfo;
import com.yzd.netty.resolver.k8s.entity.K8sServicePort;
import com.yzd.netty.resolver.k8s.entity.K8sServiceSubsets;
import com.yzd.netty.resolver.utils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

import static com.yzd.netty.resolver.k8s.RequestType.QUERY;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverQueryChannelHandler extends SimpleChannelInboundHandler {

    private static final int SUCCESS_CODE = 200;
    private final K8sResolverProvider resolverProvider;
    private final RequestType requestType;
    private final URI uri;
    private long resourceVersion = 0L;

    public K8sResolverQueryChannelHandler(K8sResolverProvider resolverProvider, RequestType requestType, URI uri) {
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
            String content = httpContent.content().toString(UTF_8);
            log.debug("HTTP_CONTENT:" + content);
            Map<String, Set<InetSocketAddress>> addressMap = parseAddress(content);
            Set<InetSocketAddress> tempNodeSet = addressMap.getOrDefault("http", Collections.emptySet());
            resolverProvider.reloadNode(tempNodeSet);
        }
        if (msg instanceof LastHttpContent) {
            log.debug("LastHttpContent");
        }
        ctx.close();
    }

    private Map<String, Set<InetSocketAddress>> parseAddress(String content) {
        K8sServiceInfo k8sServiceInfo = JsonUtils.toJavaObject(content, K8sServiceInfo.class);
        if (k8sServiceInfo.getSubsets() == null || k8sServiceInfo.getSubsets().isEmpty()) {
            log.warn("No valid address,resolver info({}).", getResolverInfo());
            return Collections.emptyMap();
        }
        Map<String, Set<InetSocketAddress>> addressMap = new HashMap<>();
        for (K8sServiceSubsets subset : k8sServiceInfo.getSubsets()) {
            for (K8sServicePort port : subset.getPorts()) {
                Set<InetSocketAddress> addressSet = addressMap.computeIfAbsent(port.getName(), key -> new HashSet<InetSocketAddress>());
                for (K8sServiceAddress address : subset.getAddresses()) {
                    addressSet.add(new InetSocketAddress(address.getIp(), port.getPort()));
                }
            }
        }
        return addressMap;
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

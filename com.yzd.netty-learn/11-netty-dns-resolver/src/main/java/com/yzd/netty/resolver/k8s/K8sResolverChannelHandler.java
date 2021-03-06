package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.k8s.entity.K8sServiceAddress;
import com.yzd.netty.resolver.k8s.entity.K8sServiceInfo;
import com.yzd.netty.resolver.k8s.entity.K8sServicePort;
import com.yzd.netty.resolver.k8s.entity.K8sServiceSubsets;
import com.yzd.netty.resolver.utils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

import static com.yzd.netty.resolver.k8s.RequestType.QUERY;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
abstract class K8sResolverChannelHandler extends SimpleChannelInboundHandler {

    protected static final int SUCCESS_CODE = 200;
    protected static final int NOT_FOUND_CODE = 404;
    protected final K8sResolverProvider resolverProvider;
    protected final RequestType requestType;
    protected final URI uri;
    private boolean isReceiveMessage = false;

    public K8sResolverChannelHandler(K8sResolverProvider resolverProvider, RequestType requestType, URI uri) {
        this.resolverProvider = resolverProvider;
        this.requestType = requestType;
        this.uri = uri;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (resolverProvider.isClosed()) {
            return;
        }
        if (!isReceiveMessage) {
            resolverProvider.parseSuccess = false;
            //没有收到任务消息。原因：可能k8s地址配置不正确
            log.error("K8s resolver connection closed! resolver info({}),{}.", getResolverInfo(), "not received any message");
        }
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpObject) {
            isReceiveMessage = true;
        }
        super.channelRead(ctx, msg);
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
                if (resolverProvider.isClosed()) {
                    ctx.close();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("K8s resolver fail! resolver info({}) , Occur an exception !", getResolverInfo(), cause);
        resolverProvider.parseSuccess = false;
        ctx.close();
    }

    protected void reloadNode(String content) {
        Map<String, Set<InetSocketAddress>> addressMap = parseAddress(content);
        //Set<InetSocketAddress> tempNodeSet = addressMap.getOrDefault("http", Collections.emptySet());
        Set<InetSocketAddress> tempNodeSet =
                addressMap.getOrDefault(resolverProvider.getTargetNode().getPortType(), Collections.emptySet());
        resolverProvider.reloadNode(tempNodeSet);
        resolverProvider.parseSuccess = true;
    }

    protected Map<String, Set<InetSocketAddress>> parseAddress(String content) {
        K8sServiceInfo k8sServiceInfo = JsonUtils.toJavaObject(content, K8sServiceInfo.class);
        if (k8sServiceInfo == null || k8sServiceInfo.getSubsets() == null || k8sServiceInfo.getSubsets().isEmpty()) {
            log.warn("K8s resolver fail! resolver info({}) ,no address ,full content:{}.", getResolverInfo(), content);
            return Collections.emptyMap();
        }
        Map<String, Set<InetSocketAddress>> addressMap = new HashMap<>();
        for (K8sServiceSubsets subset : k8sServiceInfo.getSubsets()) {
            //处理：subsets":[{"notReadyAddresses":………………的情况
            if (subset.getPorts() == null || subset.getAddresses() == null) {
                continue;
            }
            for (K8sServicePort port : subset.getPorts()) {
                Set<InetSocketAddress> addressSet =
                        addressMap.computeIfAbsent(port.getName(), key -> new HashSet<InetSocketAddress>());
                for (K8sServiceAddress address : subset.getAddresses()) {
                    addressSet.add(new InetSocketAddress(address.getIp(), port.getPort()));
                }
            }
        }
        return addressMap;
    }

    protected String getResolverInfo() {
        StringBuilder nodeInfo = new StringBuilder();
        nodeInfo.append("request type:").append(requestType)
                .append(",uri:").append(uri);
        return nodeInfo.toString();
    }
}

package com.yzd.netty.resolver.raw;

import com.google.common.collect.Sets;
import com.yzd.netty.resolver.BaseResolverProvider;
import com.yzd.netty.resolver.config.TargetNode;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @Author: yaozh
 * @Description:
 */
public class RawResolverProvider extends BaseResolverProvider {
    public RawResolverProvider(TargetNode targetNode) {
        super(targetNode);
        init(targetNode);
    }

    private void init(TargetNode targetNode) {
        Set<InetSocketAddress> newNodeSet = Sets.newHashSet(
                new InetSocketAddress(targetNode.getHost(), targetNode.getPort()));
        reloadNode(newNodeSet);
    }

    @Override
    public void shutdown() {
        close();
    }
}

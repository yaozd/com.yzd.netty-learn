package com.yzd.netty.resolver;

import com.google.common.collect.Sets;
import com.yzd.netty.resolver.config.TargetNode;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yaozh
 * @Description:
 */
public abstract class BaseResolverProvider implements ResolverProvider {

    private static final int MAX_REWIND_SIZE = 1000000;
    private AtomicInteger currentCount = new AtomicInteger(0);
    protected TargetNode targetNode;
    @Getter
    private Set<InetSocketAddress> nodeSet = new HashSet<>(10);
    private InetSocketAddress[] nodeArray = {};
    private volatile boolean isClose = false;

    protected BaseResolverProvider(TargetNode targetNode) {
        this.targetNode = targetNode;
    }

    @Override
    public InetSocketAddress doSelect() {
        if (null == nodeArray || nodeArray.length == 0) {
            return null;
        }
        int nodeSize = nodeArray.length;
        if (nodeSize == 1) {
            return nodeArray[0];
        }
        int index = currentCount.incrementAndGet();
        if (index > MAX_REWIND_SIZE) {
            index = 0;
            currentCount.set(index);
        }
        return nodeArray[index % nodeSize];
    }

    @Override
    public boolean isEnable(InetSocketAddress address) {
        return nodeSet.contains(address);
    }

    @Override
    public void reloadNode(Set<InetSocketAddress> newNodeSet) {
        Sets.SetView<InetSocketAddress> diff = Sets.union(nodeSet, newNodeSet);
        if (diff.size() > 0) {
            nodeSet = newNodeSet;
            nodeArray = newNodeSet.toArray(new InetSocketAddress[newNodeSet.size()]);
        }
        //保证在多线情况下正常清除数据
        if (isClose) {
            clearData();
        }
    }

    protected void close() {
        isClose = true;
        clearData();
    }

    private void clearData() {
        nodeSet = Collections.emptySet();
        nodeArray = new InetSocketAddress[]{};
    }
}

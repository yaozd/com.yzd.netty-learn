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
        try {
            return getNode();
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    private InetSocketAddress getNode() {
        int nodeSize = nodeArray.length;
        if (nodeSize == 0) {
            return null;
        }
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
    public boolean isAvailable(InetSocketAddress address) {
        return nodeSet.contains(address);
    }

    @Override
    public void reloadNode(Set<InetSocketAddress> newNodeSet) {
        Sets.SetView<InetSocketAddress> oldDifference = Sets.difference(nodeSet, newNodeSet);
        Sets.SetView<InetSocketAddress> newDifference = Sets.difference(newNodeSet, nodeSet);
        if (oldDifference.size() > 0 || newDifference.size() > 0) {
            nodeArray = newNodeSet.toArray(new InetSocketAddress[newNodeSet.size()]);
            nodeSet = newNodeSet;
        }
        //保证在多线情况下正常清除数据
        if (isClose) {
            clearData();
        }
    }

    @Override
    public Set<InetSocketAddress> getAllNode() {
        return nodeSet;
    }

    protected void close() {
        isClose = true;
        clearData();
    }

    private void clearData() {
        nodeArray = new InetSocketAddress[]{};
        nodeSet = Collections.emptySet();
    }
}

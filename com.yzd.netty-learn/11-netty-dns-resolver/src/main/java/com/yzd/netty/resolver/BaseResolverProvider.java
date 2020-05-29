package com.yzd.netty.resolver;

import com.google.common.collect.Sets;
import com.yzd.netty.resolver.config.TargetNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public abstract class BaseResolverProvider implements ResolverProvider {

    private static final int MAX_REWIND_SIZE = 1000000;
    @Getter
    protected TargetNode targetNode;
    private AtomicInteger currentCount = new AtomicInteger(0);
    @Getter
    private volatile Set<InetSocketAddress> nodeSet = new HashSet<>(10);
    private volatile InetSocketAddress[] nodeArray = {};
    @Getter
    private volatile boolean isClosed;

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
            if (isClosed) {
                return;
            }
            updateNodeSet(newNodeSet);
            if (log.isDebugEnabled()) {
                log.debug("NODE_SIZE:{}", nodeSet.size());
            }
        }
    }

    private void updateNodeSet(Set<InetSocketAddress> newNodeSet) {
        nodeArray = newNodeSet.toArray(new InetSocketAddress[newNodeSet.size()]);
        nodeSet = newNodeSet;
    }

    @Override
    public Set<InetSocketAddress> getAllNode() {
        return nodeSet;
    }

    @Override
    public void shutdown() {
        cancel();
        clearData();
    }

    private void clearData() {
        isClosed = true;
        updateNodeSet(Collections.emptySet());
    }
}

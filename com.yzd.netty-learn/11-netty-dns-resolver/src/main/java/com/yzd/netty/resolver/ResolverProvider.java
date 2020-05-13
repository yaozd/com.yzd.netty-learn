package com.yzd.netty.resolver;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @Author: yaozh
 * @Description:
 */
public interface ResolverProvider {
    /**
     * select target node
     *
     * @return
     */
    InetSocketAddress doSelect();

    /**
     * target node is enable
     *
     * @param address
     * @return
     */
    boolean isEnable(InetSocketAddress address);

    /**
     * reload target node
     *
     * @param newNodeSet
     */
    void reloadNode(Set<InetSocketAddress> newNodeSet);

    /**
     * shutdown
     */
    void shutdown();
}

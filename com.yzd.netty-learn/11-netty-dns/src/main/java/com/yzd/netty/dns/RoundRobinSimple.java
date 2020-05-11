package com.yzd.netty.dns;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡-简单轮询
 * @author: yaozhendong
 * @create: 2019-10-11 17:32
 **/

public class RoundRobinSimple {
    private static final int MAX_REWIND_SIZE = 1000000;
    private AtomicInteger currentCount = new AtomicInteger(0);

    public InetSocketAddress doSelect(InetSocketAddress[] nodeList) {
        if (null == nodeList||nodeList.length==0) {
            return null;
        }
        int nodeSize = nodeList.length;
        if (nodeSize == 1) {
            return nodeList[0];
        }
        int index = currentCount.incrementAndGet();
        if (index > MAX_REWIND_SIZE) {
            index = 0;
            currentCount.set(index);
        }
        return nodeList[index % nodeSize];
    }

}

package com.yzd.netty.dns;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * 模拟配置变更操作
 * 负载均衡-简单轮询
 *
 * @Author: yaozh
 * @Description:
 */
public class CollectionAddressTest {

    @Test
    public void collectionAddressTest() {
        Set<InetSocketAddress> set1 = new HashSet<>();
        Set<InetSocketAddress> set2 = new HashSet<>();
        set1.add(new InetSocketAddress("127.0.0.3", 100));
        set1.add(new InetSocketAddress("127.0.0.2", 100));
        set1.add(new InetSocketAddress("127.0.0.1", 100));
        set2.add(new InetSocketAddress("127.0.0.2", 100));
        set2.add(new InetSocketAddress("127.0.0.3", 100));
        set2.add(new InetSocketAddress("127.0.0.4", 100));
        set2.add(new InetSocketAddress("127.0.0.5", 100));
        set2.add(new InetSocketAddress("127.0.0.6", 100));
        set2.add(new InetSocketAddress("127.0.0.7", 100));
        set2.add(new InetSocketAddress("127.0.0.8", 100));
        set2.add(new InetSocketAddress("127.0.0.9", 100));
        set2.add(new InetSocketAddress("127.0.0.10", 100));
        set2.add(new InetSocketAddress("127.0.0.11", 100));
        set2.add(new InetSocketAddress("127.0.0.12", 100));
        //交集
        Sets.SetView<InetSocketAddress> inter = Sets.intersection(set1, set2);
        //差集
        Sets.SetView<InetSocketAddress> diff = Sets.difference(set1, set2);
        System.out.println("diff size:"+diff.size());
        //并集
        Sets.SetView<InetSocketAddress> union = Sets.union(set1, set2);
        //
        String key = "namespace";
        Map<String, InetSocketAddress[]> addressMap = new HashMap<>();
        if (diff.size() > 0) {
            addressMap.put(key, set2.toArray(new InetSocketAddress[set2.size()]));
        }
        InetSocketAddress[] nodes = addressMap.get(key);
        if (nodes == null || 0 == nodes.length) {
            System.out.println("not found address");
            //empty array
            InetSocketAddress[] addresses = {};
            return;
        }
        InetSocketAddress address = nodes[1];
        System.out.println(address);
        RoundRobinSimple roundRobinSimple = new RoundRobinSimple();
        for (int i = 0; i < 10000000; i++) {
            InetSocketAddress inetSocketAddress = roundRobinSimple.doSelect(nodes);
            //System.out.println(inetSocketAddress);
        }
        //配置更新后，关闭已经删除的节点的连接
        Map<String,Set<InetSocketAddress>> validNodes=new HashMap<>();
        validNodes.put(key,set2);
        if(validNodes==null){
            System.out.println("not found nodes");
            return;
        }
        Set<InetSocketAddress> nodeSet=validNodes.get(key);
        for (int i = 0; i < 10000000; i++) {
            boolean contains = nodeSet.contains(address);
            //System.out.println(contains);
        }
    }

    @Test
    public void listTest() {
        List<InetSocketAddress> inetSocketAddressList=new ArrayList<>();
    }

}

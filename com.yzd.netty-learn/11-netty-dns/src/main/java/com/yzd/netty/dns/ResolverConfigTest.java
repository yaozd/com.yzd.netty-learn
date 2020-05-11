package com.yzd.netty.dns;

import com.yzd.netty.dns.config.JndiContextResolverConfigProvider;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: yaozh
 * @Description:
 */
public class ResolverConfigTest {
    /**
     * 获取本机默认的DNS配置信息
     * 参考 dnsjava
     * https://mvnrepository.com/artifact/dnsjava/dnsjava
     * <dependency>
     *     <groupId>dnsjava</groupId>
     *     <artifactId>dnsjava</artifactId>
     *     <version>3.0.2</version>
     * </dependency>
     * class:
     * org.xbill.DNS.ResolverConfig
     * org.xbill.DNS.config.JndiContextResolverConfigProvider
     */
    @Test
    public void getDefaultNameServerTest() {
        JndiContextResolverConfigProvider resolverConfigProvider=new JndiContextResolverConfigProvider();
        resolverConfigProvider.initialize();
        List<InetSocketAddress> servers = resolverConfigProvider.servers();
        for (InetSocketAddress server : servers) {
            System.out.println(server.toString());
        }
        InetSocketAddress firstAddress = resolverConfigProvider.getFirstAddress();
        if(firstAddress==null){
            throw new NullPointerException("not found name server");
        }
        System.out.println(firstAddress.toString());
    }
}

package com.yzd.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.resolver.dns.DefaultDnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class DnsCacheTest {
    /**
     * https://github.com/netty/netty/tree/4.1/resolver-dns
     * https://github.com/netty/netty/blob/4.1/resolver-dns/src/test/java/io/netty/resolver/dns/DefaultDnsCacheTest.java
     *
     * @throws UnknownHostException
     */
    @Test
    public void dnsCacheTest() throws UnknownHostException {
        String hostName = "netty.io";
        EventLoopGroup Group = new NioEventLoopGroup();
        EventLoop loop = Group.next();
        final DefaultDnsCache cache = new DefaultDnsCache();
        InetAddress addr1 = InetAddress.getByAddress(new byte[]{10, 0, 0, 1});
        InetAddress addr2 = InetAddress.getByAddress(new byte[]{10, 0, 0, 2});
        cache.cache("netty.io", null, addr1, 1, loop);
        cache.cache("netty.io", null, addr2, 10000, loop);
        List<? extends DnsCacheEntry> dnsCacheEntries = cache.get(hostName, null);
        for (DnsCacheEntry dnsCacheEntry : dnsCacheEntries) {
            log.info(dnsCacheEntry.address().toString());
        }
    }

}

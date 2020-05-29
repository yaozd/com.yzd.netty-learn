package com.yzd.netty.resolver.dns;

import ch.qos.logback.classic.LoggerContext;
import cn.hutool.core.thread.ThreadUtil;
import com.yzd.netty.resolver.config.DnsServerConfig;
import com.yzd.netty.resolver.config.TargetNode;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class DnsResolverProviderTest {
    private static TargetNode targetNode = new TargetNode();
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @BeforeClass
    public static void runBeforeTestMethod() {
        targetNode.setHost("dns.test.hualala.com");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("root");
        //logger.setLevel(Level.toLevel("INFO"));

    }

    @Test
    public void getDnsServerTest() {

        DnsServerConfig dnsServerConfig = new DnsServerConfig();
        dnsServerConfig.setHostname("8.8.8.8");
        DnsResolverProvider provider = new DnsResolverProvider(null, targetNode);
        System.out.println(provider.getDnsServer());

    }

    @Test
    @PerfTest(threads = 1, invocations = 3000)
    public void queryDNSTest() {
        DnsServerConfig dnsServerConfig = new DnsServerConfig();
        dnsServerConfig.setHostname("8.8.8.81");
        TargetNode targetNode = new TargetNode();
        targetNode.setHost("dns.test.hualala.com");
        //targetNode.setHost("www.hualala.com");
        DnsResolverProvider provider = new DnsResolverProvider(null, targetNode);
        provider.queryDNS();
        ThreadUtil.sleep(1000 * 1);
        log.info("node set size:{}", provider.getNodeSet().size());
    }

    @Test
    @PerfTest(threads = 5, invocations = 10)
    public void queryDNSMultiThreadsTest() {
        DnsResolverProvider provider = new DnsResolverProvider(null, targetNode);
        provider.queryDNS();
        TargetNode targetNode1 = new TargetNode();
        targetNode1.setHost("www.baidu.com");
        DnsResolverProvider provider2 = new DnsResolverProvider(null, targetNode1);
        provider2.queryDNS();
        ThreadUtil.sleep(1000 * 1);
        log.info("node set size:{}", provider.getNodeSet().size());
        for (InetSocketAddress inetSocketAddress : provider.getNodeSet()) {
            log.info(inetSocketAddress.toString());
        }
        log.info("node set size:{}", provider2.getNodeSet().size());
        for (InetSocketAddress inetSocketAddress : provider2.getNodeSet()) {
            log.info(inetSocketAddress.toString());
        }
        log.info("node set size:====================================");

    }

    @Test
    //@PerfTest(threads = 5,invocations = 10)
    public void selectTargetNodeTest() {
        DnsServerConfig dnsServerConfig = new DnsServerConfig();
        //dnsServerConfig.setHostname("114.114.114.114");
        DnsResolverProvider provider = new DnsResolverProvider(dnsServerConfig, targetNode);
        provider.queryDNS();
        ThreadUtil.sleep(1000 * 10);
        for (int i = 0; i < 10; i++) {
            InetSocketAddress inetSocketAddress = provider.doSelect();
            log.info("address:{}", inetSocketAddress);
        }
    }

}
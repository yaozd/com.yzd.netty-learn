package com.yzd.netty.resolver.dns;

import ch.qos.logback.classic.LoggerContext;
import cn.hutool.core.thread.ThreadUtil;
import com.yzd.netty.resolver.ResolverProvider;
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
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class DnsResolverSelectNodeTest {
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();
    private static TargetNode targetNode = new TargetNode();
    private static ResolverProvider resolverProvider;
    @BeforeClass
    public static void runBeforeTestMethod() {
        targetNode.setHost("dns.test.hualala.com");
        DnsServerConfig dnsServerConfig=new DnsServerConfig();
        //dnsServerConfig.setHostname("192.168.56.112");
        resolverProvider=new DnsResolverProvider(dnsServerConfig,targetNode);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("root");
        //logger.setLevel(Level.toLevel("INFO"));
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
        ThreadUtil.newSingleExecutor().submit(()->{
            ThreadUtil.sleep(10, TimeUnit.SECONDS);
            resolverProvider.shutdown();
        });
    }
    @Test
    //@PerfTest(threads = 5,invocations = 1000000)
    @PerfTest(threads = 5,duration = 30000)
    public void selectTargetNodeTest() {
        InetSocketAddress inetSocketAddress = resolverProvider.doSelect();
        resolverProvider.isEnable(inetSocketAddress);
        //log.info("address:{}", inetSocketAddress);
        //ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
}

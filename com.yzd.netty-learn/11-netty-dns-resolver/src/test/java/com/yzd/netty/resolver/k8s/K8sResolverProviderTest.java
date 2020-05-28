package com.yzd.netty.resolver.k8s;

import cn.hutool.core.thread.ThreadUtil;
import com.yzd.netty.resolver.config.TargetNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverProviderTest {
    private static TargetNode targetNode = new TargetNode();

    @BeforeClass
    public static void beforeTest() {
        targetNode.setProtocol("http");
        targetNode.setHost("192.168.56.102");
        targetNode.setPort(8080);
        targetNode.setServicePath("default/my-nginx");
    }

    @Test
    public void queryTest() {
        K8sResolverProvider resolverProvider = new K8sResolverProvider(targetNode);
        resolverProvider.query();
        ThreadUtil.sleep(1000 * 500000);
    }

    @Test
    public void watchTest() {
        K8sResolverProvider resolverProvider = new K8sResolverProvider(targetNode);
        resolverProvider.watch();
        ThreadUtil.sleep(1000 * 500000);
    }

}
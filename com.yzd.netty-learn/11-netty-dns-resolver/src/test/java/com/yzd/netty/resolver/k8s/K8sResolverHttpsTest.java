package com.yzd.netty.resolver.k8s;

import cn.hutool.core.thread.ThreadUtil;
import com.yzd.netty.resolver.config.K8sConfig;
import com.yzd.netty.resolver.config.K8sTokenConfig;
import com.yzd.netty.resolver.config.TargetNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverHttpsTest {

    private static TargetNode targetNode = new TargetNode();

    @BeforeClass
    public static void beforeTest() {
        String host = "192.168.56.102";
        String tokenPath = "\\data\\token";
        K8sConfig k8sConfig = new K8sConfig();
        k8sConfig.getTokens().add(new K8sTokenConfig(host, tokenPath));
        K8sTokenStorage.init(k8sConfig);
        targetNode.setProtocol("https");
        targetNode.setHost(host);
        targetNode.setPort(6443);
        targetNode.setServicePath("default/my-nginx");
        targetNode.setPortType("http");
    }

    @Test
    public void queryTest() {
        K8sResolverProvider resolverProvider = new K8sResolverProvider(targetNode);
        resolverProvider.query();
        ThreadUtil.sleep(1000 * 5000000);
    }

    @Test
    public void watchTest() {
        K8sResolverProvider resolverProvider = new K8sResolverProvider(targetNode);
        resolverProvider.watch();
        ThreadUtil.sleep(1000 * 500000);
    }

}
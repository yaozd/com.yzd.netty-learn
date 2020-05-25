package com.yzd.netty.resolver.k8s;

import cn.hutool.core.thread.ThreadUtil;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
public class K8sResolverProviderTest {

    @Test
    public void queryTest() {
        K8sResolverProvider resolverProvider=new K8sResolverProvider(null);
        resolverProvider.query();
        ThreadUtil.sleep(1000*500000);
    }

    @Test
    public void watchTest() {
        K8sResolverProvider resolverProvider=new K8sResolverProvider(null);
        resolverProvider.watch();
        ThreadUtil.sleep(1000*500000);
    }
}
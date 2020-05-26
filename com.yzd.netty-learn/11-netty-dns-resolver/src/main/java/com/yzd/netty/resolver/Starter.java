package com.yzd.netty.resolver;

import com.yzd.netty.resolver.k8s.K8sResolverProvider;

/**
 * @Author: yaozh
 * @Description:
 */
public class Starter {
    public static void main(String[] args) {
        System.out.println("start main");
        K8sResolverProvider resolverProvider=new K8sResolverProvider(null);
        resolverProvider.query();
    }
}

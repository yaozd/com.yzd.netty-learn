package com.yzd.netty.resolver;

import com.yzd.netty.resolver.config.TargetNode;
import com.yzd.netty.resolver.k8s.K8sResolverProvider;

/**
 * @Author: yaozh
 * @Description:
 */
public class Starter {

    private static TargetNode targetNode = new TargetNode();

    public static void main(String[] args) {
        System.out.println("start main");
        //
        targetNode.setProtocol("http");
        targetNode.setHost("192.168.56.102");
        targetNode.setPort(8080);
        targetNode.setServicePath("default/temp-nginx");
        //
        K8sResolverProvider resolverProvider = new K8sResolverProvider(targetNode);
        resolverProvider.query();
    }
}

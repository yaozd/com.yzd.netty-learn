package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.BaseResolverProvider;
import com.yzd.netty.resolver.config.TargetNode;

/**
 * @Author: yaozh
 * @Description:
 */
public class K8sResolverProvider extends BaseResolverProvider implements Runnable {
    protected K8sResolverProvider(TargetNode targetNode) {
        super(targetNode);
    }


    @Override
    public void cancel() {

    }

    @Override
    public void run() {

    }
}

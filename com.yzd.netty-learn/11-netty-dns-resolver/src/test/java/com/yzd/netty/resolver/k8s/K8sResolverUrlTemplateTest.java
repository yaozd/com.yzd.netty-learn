package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.config.TargetNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class K8sResolverUrlTemplateTest {
    private static final String QUERY_URL_TEMPLATE = "%s://%s:%d/api/v1/namespaces/%s/endpoints/%s";
    private static final String WATCH_URL_TEMPLATE = "%s://%s:%d/api/v1/watch/namespaces/%s/endpoints/%s";

    @Test
    public void urlTemplateTest() {
        TargetNode targetNode = new TargetNode();
        targetNode.setProtocol("http");
        targetNode.setHost("192.168.56.102");
        targetNode.setPort(8080);
        targetNode.setServicePath("default/my-nginx");
        String[] pathSplits = StringUtils.split(targetNode.getServicePath(), "/");
        if (pathSplits == null || pathSplits.length != 2) {
            throw new IllegalArgumentException("Target node service path invalid ! service path:" + targetNode.getServicePath());
        }
        String queryUrl = String.format(QUERY_URL_TEMPLATE, targetNode.getProtocol(), targetNode.getHost(), targetNode.getPort(), pathSplits[0], pathSplits[1]);
        log.debug(queryUrl);
        String watchUrl = String.format(WATCH_URL_TEMPLATE, targetNode.getProtocol(), targetNode.getHost(), targetNode.getPort(), pathSplits[0], pathSplits[1]);
        log.debug(watchUrl);
    }
}

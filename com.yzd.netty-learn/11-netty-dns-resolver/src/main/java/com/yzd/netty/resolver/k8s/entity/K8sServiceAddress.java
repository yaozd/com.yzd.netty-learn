package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

@Data
public class K8sServiceAddress {

    private String ip;

    private String nodeName;

    private K8sServiceTargetRef targetRef;
}

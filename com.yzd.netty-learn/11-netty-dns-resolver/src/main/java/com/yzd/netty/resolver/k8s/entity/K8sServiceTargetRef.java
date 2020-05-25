package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

@Data
public class K8sServiceTargetRef {

    private String kind;

    private String namespace;

    private String name;

    private String uid;

    private String resourceVersion;
}

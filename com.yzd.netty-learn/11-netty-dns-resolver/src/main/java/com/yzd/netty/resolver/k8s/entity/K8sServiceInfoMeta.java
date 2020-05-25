package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

@Data
public class K8sServiceInfoMeta {

    private String name;

    private String namespace;

    private String selfLink;

    private String uid;

    private String creationTimestamp;

    private String resourceVersion;

    private K8sServiceInfoMetaLabels labels;
}

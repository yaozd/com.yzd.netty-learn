package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

@Data
public class K8sServicePort {

    public K8sServicePort() {
        name = "grpc";
    }

    private String name;

    private int port;

    private String protocol;
}

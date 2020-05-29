package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

@Data
public class K8sServicePort {

    private String name;
    private int port;
    private String protocol;

    public K8sServicePort() {
        name = "grpc";
    }
}

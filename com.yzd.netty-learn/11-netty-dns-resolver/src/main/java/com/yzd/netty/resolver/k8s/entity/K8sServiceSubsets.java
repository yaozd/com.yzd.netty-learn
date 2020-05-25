package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

import java.util.List;

@Data
public class K8sServiceSubsets {

    private List<K8sServiceAddress> addresses;

    private List<K8sServicePort> ports;
}

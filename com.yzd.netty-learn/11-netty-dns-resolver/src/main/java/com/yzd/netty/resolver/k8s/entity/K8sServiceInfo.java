package com.yzd.netty.resolver.k8s.entity;

import lombok.Data;

import java.util.List;

@Data
public class K8sServiceInfo {

    private String kind;

    private String apiVersion;

    private K8sServiceInfoMeta metadata;

    private List<K8sServiceSubsets> subsets;
}


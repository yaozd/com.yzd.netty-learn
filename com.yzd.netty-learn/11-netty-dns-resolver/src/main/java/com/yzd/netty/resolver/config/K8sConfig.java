package com.yzd.netty.resolver.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: yaozh
 * @Description:
 */
@Getter
public class K8sConfig {
    @Setter
    List<K8sTokenConfig> tokens = new ArrayList<>();
}

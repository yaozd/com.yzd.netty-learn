package com.yzd.netty.resolver.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: yaozh
 * @Description:
 */
@Getter
public class K8sTokenConfig {
    @Setter
    public String host;
    @Setter
    public String tokenPath;

    public K8sTokenConfig(String host, String tokenPath) {
        this.host = host;
        this.tokenPath = tokenPath;
    }
}

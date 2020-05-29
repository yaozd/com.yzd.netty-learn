package com.yzd.netty.resolver.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @Author: yaozh
 * @Description:
 */
@Getter
public class TargetNode {
    @Setter
    public String protocol;
    @Setter
    public String host;
    @Setter
    public int port = 80;
    @Setter
    public String servicePath;
    @Setter
    public String portType = "http";

    @Getter
    public String configVersion = UUID.randomUUID().toString();
}

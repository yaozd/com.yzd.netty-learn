package com.yzd.netty.resolver.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @Author: yaozh
 * @Description:
 */
public class TargetNode {
    @Getter@Setter
    public String host;
    @Getter@Setter
    public int port=80;

    @Getter
    public String configVersion=UUID.randomUUID().toString();
}

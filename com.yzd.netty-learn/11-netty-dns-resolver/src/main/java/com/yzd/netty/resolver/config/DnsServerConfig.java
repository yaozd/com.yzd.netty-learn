package com.yzd.netty.resolver.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: yaozh
 * @Description:
 */

public class DnsServerConfig {
    @Getter@Setter
    private String hostname;
    @Getter@Setter
    private int port=53;
}

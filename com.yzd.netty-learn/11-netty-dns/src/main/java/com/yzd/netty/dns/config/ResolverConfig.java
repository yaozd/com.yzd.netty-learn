package com.yzd.netty.dns.config;

import java.net.InetSocketAddress;

/**
 * @Author: yaozh
 * @Description:
 */
public class ResolverConfig {
    private final static JndiContextResolverConfigProvider jndiContextResolverConfigProvider;
    public static InetSocketAddress getDnsServerAddress(){
       return jndiContextResolverConfigProvider.getFirstAddress();
    }
    static {
        jndiContextResolverConfigProvider=new JndiContextResolverConfigProvider();
        jndiContextResolverConfigProvider.initialize();
    }
}

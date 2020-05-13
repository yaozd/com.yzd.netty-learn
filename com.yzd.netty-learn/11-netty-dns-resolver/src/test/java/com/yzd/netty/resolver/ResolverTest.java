package com.yzd.netty.resolver;

import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
public class ResolverTest {
    @Test
    public void resolverTest() {
        Resolver resolver=new Resolver();
        resolver.doSelect();
    }
}
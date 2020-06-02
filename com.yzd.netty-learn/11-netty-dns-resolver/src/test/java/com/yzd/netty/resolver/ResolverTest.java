package com.yzd.netty.resolver;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ResolverTest {
    @Test
    public void resolverTest() {
        Resolver resolver = new Resolver();
        resolver.doSelect();
    }

    @Test
    public void logTest() {
        String nullObj=null;
        log.error("null object:{}",nullObj);
        log.error(String.format("null object:%s",nullObj));
    }
}
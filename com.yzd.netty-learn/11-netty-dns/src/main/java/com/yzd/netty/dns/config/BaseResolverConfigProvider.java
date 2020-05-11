package com.yzd.netty.dns.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: yaozh
 * @Description:
 */
abstract class BaseResolverConfigProvider {
    final Logger log = LoggerFactory.getLogger(getClass());
    List<InetSocketAddress> nameservers = new ArrayList<>(3);
    void addNameServer(InetSocketAddress server) {
        if (!nameservers.contains(server)) {
            nameservers.add(server);
            log.debug("Added {} to nameservers", server);
        }
    }
    public final List<InetSocketAddress> servers() {
        return Collections.unmodifiableList(nameservers);
    }
}

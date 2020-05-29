package com.yzd.netty.resolver.dns;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
public class DnsResolverChannelInitializer extends ChannelInitializer<DatagramChannel> {

    private final DnsResolverProvider resolverProvider;

    public DnsResolverChannelInitializer(DnsResolverProvider resolverProvider) {
        this.resolverProvider = resolverProvider;
    }

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new DatagramDnsQueryEncoder())
                .addLast(new DatagramDnsResponseDecoder())
                .addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS))
                .addLast(new DnsResolverChannelHandler(resolverProvider));
    }
}

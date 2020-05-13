package com.yzd.netty.resolver.dns;

import com.yzd.netty.resolver.BaseResolverProvider;
import com.yzd.netty.resolver.config.DnsServerConfig;
import com.yzd.netty.resolver.config.TargetNode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class DnsResolverProvider extends BaseResolverProvider {
    private final DnsServerConfig dnsServerConfig;

    public DnsResolverProvider(DnsServerConfig dnsServerConfig, TargetNode targetNode) {
        this.dnsServerConfig = dnsServerConfig;
        this.targetNode = targetNode;
    }

    public void queryDNS() {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new DnsResolverChannelInitializer(this));
        b.bind(0).addListener(new DnsResolverConnectFutureListener(this));
    }

    public InetSocketAddress getDnsServer() {
        if (dnsServerConfig != null && dnsServerConfig.getHostname() != null) {
            return new InetSocketAddress(dnsServerConfig.getHostname(), dnsServerConfig.getPort());
        }
        return JndiContextResolverConfigProvider.getInstance().getFirstNameServer();
    }

    public int getTargetNodePort() {
        return targetNode.getPort();
    }

    public String getTargetNodeHost() {
        return targetNode.getHost();
    }


    static class DnsResolverConnectFutureListener implements GenericFutureListener<ChannelFuture> {

        private final DnsResolverProvider resolverProvider;

        public DnsResolverConnectFutureListener(DnsResolverProvider resolverProvider) {
            this.resolverProvider = resolverProvider;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
                future.channel().close();
                log.error("Connect to dns server failed, node info {}",
                        resolverProvider.getDnsServer().toString(), future.cause());
                return;
            }
            Channel newChannel = future.channel();
            DnsQuery query = new DatagramDnsQuery(null, resolverProvider.getDnsServer(), 1)
                    .setRecord(DnsSection.QUESTION, new DefaultDnsQuestion(resolverProvider.targetNode.getHost(), DnsRecordType.A));
            newChannel.writeAndFlush(query);
            log.info("connect success");
        }
    }
}

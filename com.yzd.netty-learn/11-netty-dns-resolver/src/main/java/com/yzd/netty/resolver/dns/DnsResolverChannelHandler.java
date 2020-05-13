package com.yzd.netty.resolver.dns;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class DnsResolverChannelHandler extends SimpleChannelInboundHandler<DatagramDnsResponse> {

    private final DnsResolverProvider resolverProvider;

    public DnsResolverChannelHandler(DnsResolverProvider resolverProvider) {
        this.resolverProvider = resolverProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) throws Exception {
        handleQueryResp(msg);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) {
                log.error("Dns resolver fail! read timeout!  ,dns server:{} ,target node host:{} .", resolverProvider.getDnsServer(), resolverProvider.getTargetNodeHost());
                ctx.close();
            }
        }
    }

    private void handleQueryResp(DatagramDnsResponse msg) {
        if (msg.count(DnsSection.QUESTION) < 1) {
            log.warn("Dns section question not found!");
            return;
        }
        if (log.isDebugEnabled()) {
            DnsQuestion question = msg.recordAt(DnsSection.QUESTION, 0);
            log.debug("name:{}", question.name());
        }
        DnsResponseCode code = msg.code();
        if (DnsResponseCode.NOERROR.equals(code)) {
            int answerCount = msg.count(DnsSection.ANSWER);
            if (answerCount == 0) {
                resolverProvider.reloadNode(Collections.EMPTY_SET);
                log.warn("Dns resolver! dns response code:NoError(0) ,but answer is null,dns server:{} ,target node host:{} .", resolverProvider.getDnsServer(), resolverProvider.getTargetNodeHost());
                return;
            }

            Set<InetSocketAddress> tempNodeSet = new HashSet<>(10);
            for (int i = 0, count = msg.count(DnsSection.ANSWER); i < count; i++) {
                DnsRecord record = msg.recordAt(DnsSection.ANSWER, i);
                if (record.type() == DnsRecordType.A) {
                    //just print the IP after query
                    DnsRawRecord raw = (DnsRawRecord) record;
                    if (log.isDebugEnabled()) {
                        log.debug("time to Live:{}", raw.timeToLive());
                    }
                    String ipAddress = NetUtil.bytesToIpAddress(ByteBufUtil.getBytes(raw.content()));
                    tempNodeSet.add(new InetSocketAddress(ipAddress, resolverProvider.getTargetNodePort()));
                    if (log.isDebugEnabled()) {
                        log.debug(ipAddress);
                    }
                }

            }
            resolverProvider.reloadNode(tempNodeSet);
            return;
        }
        if (DnsResponseCode.NXDOMAIN.equals(code)) {
            resolverProvider.reloadNode(Collections.EMPTY_SET);
            log.warn("Dns resolver! dns response code:NXDomain(3) ,not exist domain,dns server:{} ,target node host:{} .", resolverProvider.getDnsServer(), resolverProvider.getTargetNodeHost());
            return;
        }
        log.error("Dns resolver fail! dns response code name:{} ,code value:{} .", code, code.intValue());
    }

}

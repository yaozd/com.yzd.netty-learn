package com.yzd.netty.dns;

/**
 * @Author: yaozh
 * @Description:
 */
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.thread.ThreadUtil;
import com.yzd.netty.dns.config.JndiContextResolverConfigProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.util.NetUtil;

public final class DnsClient {

    private static final String QUERY_DOMAIN = "dns.test.hualala.com";
    //private static final String QUERY_DOMAIN = "blackpiglet.com";
    private static final int DNS_SERVER_PORT = 53;
    //private static final String DNS_SERVER_HOST = "192.168.56.112";
    private static final String DNS_SERVER_HOST = "172.16.0.75";
    private static final InetSocketAddress DEFAULT_DNS_SERVER;
    //private static final String DNS_SERVER_HOST = "8.8.8.8";
    static {
        JndiContextResolverConfigProvider resolverConfigProvider=new JndiContextResolverConfigProvider();
        resolverConfigProvider.initialize();
        DEFAULT_DNS_SERVER=resolverConfigProvider.getFirstAddress();
    }
    private DnsClient() {

    }

    private static void handleQueryResp(DatagramDnsResponse msg) {
        if (msg.count(DnsSection.QUESTION) > 0) {
            DnsQuestion question = msg.recordAt(DnsSection.QUESTION, 0);
            System.out.printf("name: %s%n", question.name());
        }
        for (int i = 0, count = msg.count(DnsSection.ANSWER); i < count; i++) {
            DnsRecord record = msg.recordAt(DnsSection.ANSWER, i);
            if (record.type() == DnsRecordType.A) {
                //just print the IP after query
                DnsRawRecord raw = (DnsRawRecord) record;
                System.out.println(NetUtil.bytesToIpAddress(ByteBufUtil.getBytes(raw.content())));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = getDnsServerAddress();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new DatagramDnsQueryEncoder())
                                    .addLast(new DatagramDnsResponseDecoder())
                                    .addLast(new SimpleChannelInboundHandler<DatagramDnsResponse>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) {
                                            try {
                                                handleQueryResp(msg);
                                            } finally {
                                                ctx.close();
                                            }
                                        }
                                    });
                        }
                    });
            for (int i = 0; i < 1000; i++) {
                ThreadUtil.sleep(1000);

            final Channel ch = b.bind(0).sync().channel();
            DnsQuery query = new DatagramDnsQuery(null, addr, 1).setRecord(
                    DnsSection.QUESTION,
                    new DefaultDnsQuestion(QUERY_DOMAIN, DnsRecordType.A));
            ch.writeAndFlush(query).sync();
            boolean succ = ch.closeFuture().await(10, TimeUnit.SECONDS);
            if (!succ) {
                System.err.println("dns query timeout!");
                ch.close().sync();
            }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    private static InetSocketAddress getDnsServerAddress() {
        if(DNS_SERVER_HOST!=null&&DNS_SERVER_HOST!=null) {
            return new InetSocketAddress(DNS_SERVER_HOST, DNS_SERVER_PORT);
        }
        return DEFAULT_DNS_SERVER;
    }
}
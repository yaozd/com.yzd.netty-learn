package com.yzd.common;

import cn.hutool.core.thread.ThreadUtil;
import io.leopard.javahost.JavaHost;
import org.junit.Test;
import org.xbill.DNS.*;

import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
public class DnsTest {
    @Test
    public void dnsCacheTest() throws UnknownHostException {
        //java.security.Security.setProperty("networkaddress.cache.ttl", "3");
        //java.security.Security.setProperty("networkaddress.cache.negative.ttl", "2");
        //System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        //System.setProperty("sun.net.spi.nameservice.provider.1","dns,dnsjava");
        for (int i = 0; i < 1000; i++) {
            InetAddress[] allByName = InetAddress.getAllByName("dns.test.hualala.com");
            for (InetAddress inetAddress : allByName) {
                System.out.println(inetAddress);
            }
            ThreadUtil.sleep(1000);
            System.out.println("===================");
        }
    }
    @Test
    public void dnsTest() throws UnknownHostException {
        java.security.Security.setProperty("networkaddress.cache.ttl", "3");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "2");
        //System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        System.setProperty("sun.net.spi.nameservice.provider.1","dns,dnsjava");
        for (int i = 0; i < 2000; i++) {
            InetSocketAddress hostAddress = new InetSocketAddress("dns.test.hualala.com", 80);
            System.out.println(hostAddress.toString());
            ThreadUtil.sleep(1000);
        }
    }

    @Test
    public void localhostTest() throws UnknownHostException {
        Map<String, String> properties=new HashMap<>();
        properties.put("www.hualala.com1","127.0.0.1");
        JavaHost.updateVirtualDns(properties);
        JavaHost.printAllVirtualDns();// 打印所有虚拟DNS记录
        InetSocketAddress hostAddress = new InetSocketAddress("www.hualala.com1", 80);
        System.out.println(hostAddress);
        InetAddress[] allByName = InetAddress.getAllByName("www.hualala.com1");
        for (InetAddress inetAddress : allByName) {
            System.out.println(inetAddress);
        }
    }

    @Test
    public void lookupTest(){
        //java.security.Security.setProperty("networkaddress.cache.ttl", "3");
        //java.security.Security.setProperty("networkaddress.cache.negative.ttl", "2");
        //System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        //System.setProperty("sun.net.spi.nameservice.provider.1","dns,dnsjava");
        for (int i = 0; i <10000 ; i++) {
            queryDNS();
            ThreadUtil.sleep(1000);
        }
    }

    private void queryDNS() {
        try{
            Record[] records =null;

            Lookup lookup = new Lookup("dns.test.hualala.com", Type.A);
            lookup.setCache(null);
            lookup.run();
            if(lookup.getResult() == Lookup.SUCCESSFUL){
                records=lookup.getAnswers();
            }else{
                System.out.println("未查询到结果!");
                return;
            }
            for (Record record : records) {
                System.out.println(record.rdataToString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void queryDNSWithResolver() {
        try{
            Record[] records =null;
            Resolver resolver = new SimpleResolver("192.168.56.112");
            resolver.setPort(53);
            Lookup lookup = new Lookup("blackpiglet.com", Type.A);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            lookup.run();
            if(lookup.getResult() == Lookup.SUCCESSFUL){
                records=lookup.getAnswers();
            }else{
                System.out.println("未查询到结果!");
                return;
            }
            for (Record record : records) {
                System.out.println(record.rdataToString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void Test() throws UnknownHostException, TextParseException {
        Resolver resolver = new SimpleResolver("192.168.36.54");
        resolver.setPort(53);
        Lookup lookup = new Lookup("www.test.com", Type.A);
        lookup.setResolver(resolver);
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(lookup.getAnswers()[0].rdataToString());
        }
    }

}

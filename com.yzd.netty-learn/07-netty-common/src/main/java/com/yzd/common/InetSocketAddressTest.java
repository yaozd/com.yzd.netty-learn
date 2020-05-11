package com.yzd.common;

import cn.hutool.core.thread.ThreadUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
public class InetSocketAddressTest {
    @Test
    public void inetSocketAddressTest() {
        //www.hualala.com/61.168.100.228:80
        InetSocketAddress hostAddress = new InetSocketAddress("www.hualala.com", 80);
        System.out.println(hostAddress.toString());
        String ip = StringUtils.substringAfter(hostAddress.toString(), "/");
        System.out.println(ip);
        ///127.0.0.1:80
        InetSocketAddress ipAddress = new InetSocketAddress("127.0.01", 80);
        System.out.println(ipAddress.toString());
        String s = StringUtils.removeStart("111" + ipAddress.toString(), "/");
        System.out.println(s);
    }

    /**
     * //设置解析成功的域名记录JVM中缓存的有效时间，JVM默认是永远有效，这样一来域名IP重定向必须重启JVM，这里修改为5秒钟有效，0表示禁止缓存，-1表示永远有效
     * java.security.Security.setProperty("networkaddress.cache.ttl", "5");
     * <p>
     * //设置解析失败的域名记录JVM中缓存的有效时间，JVM默认是10秒，0表示禁止缓存，-1表示永远有效
     * <p>
     * java.security.Security.setProperty("networkaddress.cache.negative.ttl","2");
     */
    @Test
    public void dnsTest() throws UnknownHostException {
        java.security.Security.setProperty("networkaddress.cache.ttl", "3");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "2");
        for (int i = 0; i < 2000; i++) {
            InetSocketAddress hostAddress = new InetSocketAddress("dns.test.hualala.com", 80);
            System.out.println(hostAddress.toString());
            ThreadUtil.sleep(1000);
            InetAddress address = InetAddress.getByName("dns.test.hualala.com");
            System.out.println(address.toString());
        }
    }

    @Test
    public void dnsCacheTest() throws UnknownHostException, NoSuchFieldException, IllegalAccessException {
        //clearCache();
        java.security.Security.setProperty("networkaddress.cache.ttl", "3");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "2");
        //System.setProperty("sun.net.inetaddr.ttl", "3");
        //System.setProperty("sun.net.inetaddr.negative.ttl", "1");
        //System.setProperty("sun.net.inetaddr.ttl", "-1");
        for (int i = 0; i < 1000; i++) {
            InetAddress[] allByName = InetAddress.getAllByName("dns.test.hualala.com");
            for (InetAddress inetAddress : allByName) {
                System.out.println(inetAddress);
            }
            ThreadUtil.sleep(1000);
            System.out.println("===================");
        }

    }
    public static void clearCache() throws NoSuchFieldException, IllegalAccessException {
        //修改缓存数据开始
        Class clazz = java.net.InetAddress.class;
        final Field cacheField = clazz.getDeclaredField("addressCache");
        cacheField.setAccessible(true);
        final Object obj = cacheField.get(clazz);
        Class cacheClazz = obj.getClass();
        final Field cachePolicyField = cacheClazz.getDeclaredField("type");
        final Field cacheMapField = cacheClazz.getDeclaredField("cache");
        cachePolicyField.setAccessible(true);
        cacheMapField.setAccessible(true);
        final Map cacheMap = (Map)cacheMapField.get(obj);
        System.out.println(cacheMap);
        cacheMap.remove("dns.test.hualala.com");
    }
}

package com.yzd;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */

public class HashMapTest {
    @Test
    public void newHashMap() {
        Map<String, String> temp = new HashMap<>();
        temp.putIfAbsent("a","b");
        temp.putIfAbsent("a","c");
        long time=System.currentTimeMillis();
        for (int i = 0; i < 100_000_000; i++) {
            //Map<String, String> map = new HashMap<>();
            Map<String, String> map = new HashMap<>();
            //Map<String, String> map = Maps.newHashMap(temp);
            //Map<String, String> map = Maps.newHashMap(temp);
        }
        System.out.println("time:"+(System.currentTimeMillis()-time));
    }
    @Test
    public void newLinkedHashMap() {
        Map<String, String> temp = new LinkedHashMap<>();
        temp.putIfAbsent("a","b");
        temp.putIfAbsent("a","c");
        long time=System.currentTimeMillis();
        for (int i = 0; i < 100_000_000; i++) {
            //Map<String, String> map = new HashMap<>();
            Map<String, String> map = new LinkedHashMap<>(temp);
            //Map<String, String> map = Maps.newHashMap(temp);
            //Map<String, String> map = Maps.newHashMap(temp);
        }
        System.out.println("time:"+(System.currentTimeMillis()-time));
    }
    @Test
    public void intTest(){
        Integer a=1;
        Integer b=2;
        long time=System.currentTimeMillis();
        for (int i = 0; i < 100_000_000; i++) {
            //boolean b1 = a.equals(b);
            boolean b1 = a == b;
        }
        System.out.println("time:"+(System.currentTimeMillis()-time));
    }
}

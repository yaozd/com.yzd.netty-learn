package com.yzd.common;

import com.google.common.collect.Maps;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class HeaderHashMapTest {
    /**
     * hashmap key大小写敏感
     */
    @Test
    public void testMapGetValue() {
        HashMap<String, String> header = Maps.newHashMap();
        header.put("groupid", "1");
        String groupid;
        groupid = header.get("groupid");
        log.info(groupid);
        groupid = header.get("groupID");
        log.info(groupid);
    }

    /**
     * HttpHeaders忽略key的大小写敏感
     */
    @Test
    public void testHttpHeaders() {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.set("t", "1");
        log.info("KEY:T,VALUE:{}", httpHeaders.get("T"));
        httpHeaders.set("groupid", "3422");
        log.info("KEY:groupID,VALUE:{}", httpHeaders.get("groupID"));
    }

    /**
     * HeaderHashMap忽略key的大小写敏感
     */
    @Test
    public void testHeaderHashMapGetValue() {
        for (int i = 0; i < 100000; i++) {
            Map<String, String> header = new HeaderHashMap();
            header.put(null, "1");
            header.put("groupid", "1");
            String groupid;
            groupid = header.get("groupid");
            log.info(groupid);
            groupid = header.get("groupID");
            log.info(groupid);
        }
    }
}
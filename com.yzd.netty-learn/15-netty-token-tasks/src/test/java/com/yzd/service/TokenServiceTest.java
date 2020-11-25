package com.yzd.service;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * @Author: yaozh
 * @Description:
 */
public class TokenServiceTest {
    private TokenService tokenService;

    @Before
    public void init() {
        tokenService = new TokenService();
    }

    @Test
    public void putTrueTokenMemSize() {
        Runtime runtime = Runtime.getRuntime();
        //内存计算
        long heapSize = runtime.totalMemory() - runtime.freeMemory();
        TokenService tokenService = new TokenService();
        for (int i = 0; i < 1_000_000; i++) {
            tokenService.putValidToken(getToken());
        }
        long heapSize2 = runtime.totalMemory() - runtime.freeMemory();
        long size = (heapSize2 - heapSize) / 1000;
        System.out.println(size);
    }

    @Test
    public void putValidToken() {
    }

    @Test
    public void putInvalidToken() {
    }

    @Test
    public void putUnknownToken() {
        TokenData d1 = new TokenData();
        TokenData d2 = new TokenData();
        tokenService.putUnknownToken(d1);
        tokenService.putUnknownToken(d2);
    }

    @Test
    public void refreshUnknownToken() {
        TokenData d1 = new TokenData();
        d1.setId("1");
        d1.setToken("token");
        tokenService.putUnknownToken(d1);
        tokenService.refreshUnknownToken(d1);
    }

    @Test
    public void tokenDataTest() {
        TokenData d1 = new TokenData();
        TokenData d2 = new TokenData();
        System.out.println(d1.equals(d2));
    }

    private String getToken() {
        return UUID.randomUUID().toString();
    }

    @Test
    public void checkToken() {
        String token = "token-xxx";
        Boolean result = tokenService.checkToken(token);
        if (result != null) {
            if (!result) {
                System.out.println("Result is false");
            }
            return;
        }
        TokenData d1 = new TokenData(token);
        d1.setId("1");
        if (result == null) {
            tokenService.putUnknownToken(d1);
            return;
        }
        System.out.println("result:" + result);
    }
}
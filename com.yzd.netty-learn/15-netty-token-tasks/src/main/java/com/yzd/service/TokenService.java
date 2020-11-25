package com.yzd.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 *
 * @Author: yaozh
 * @Description:
 */
public class TokenService {
    private static final Boolean VALID = true;
    private static final Boolean INVALID = false;
    private static final Object UNKNOWN = new Object();
    private final Cache<String, Boolean> validTokenCache;
    private final Cache<String, Boolean> invalidTokenCache;
    private final LoadingCache<TokenData, Object> unknownTokenCache;

    public TokenService() {
        validTokenCache = Caffeine.newBuilder()
                .initialCapacity(0)//初始大小
                .maximumSize(500_000)//最大数量 ,PS:最大容量，不是一个精确数据，如果是小幅度超过最大容量时，数据不一定会被删除
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
        invalidTokenCache = Caffeine.newBuilder()
                .initialCapacity(0)//初始大小
                .maximumSize(100_000)//最大数量 ,PS:最大容量，不是一个精确数据，如果是小幅度超过最大容量时，数据不一定会被删除
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
        unknownTokenCache = Caffeine.newBuilder()
                .initialCapacity(0)//初始大小
                .maximumSize(10_000)//最大数量 ,PS:最大容量，不是一个精确数据，如果是小幅度超过最大容量时，数据不一定会被删除
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build(new CacheLoader<TokenData, Object>() {
                    @Override
                    public Object load(TokenData data) {
                        System.out.println(data.toString());
                        return UNKNOWN;
                    }
                });
    }

    public void putValidToken(String token) {
        validTokenCache.put(token, VALID);
    }

    public void putInvalidToken(String token) {
        invalidTokenCache.put(token, INVALID);
    }

    public void putUnknownToken(TokenData token) {
        checkTokenData(token);
        unknownTokenCache.get(token);
    }

    /**
     * 当连接断开时，可以通过刷新再次发起请求
     *
     * @param data
     */
    public void refreshUnknownToken(TokenData data) {
        if (data == null) {
            return;
        }
        unknownTokenCache.refresh(data);
    }

    public Boolean checkToken(String token) {
        Boolean result = validTokenCache.getIfPresent(token);
        return result != null ? result : invalidTokenCache.getIfPresent(token);
    }

    /**
     * 请求数据结构验证
     *
     * @param data
     */
    public void checkTokenData(TokenData data) {
        if (data == null) {
            throw new TokenServiceException("TokenData is null");
        }
        if (StringUtils.isBlank(data.getToken())) {
            throw new TokenServiceException("TokenData.token is null");
        }
        if (StringUtils.isBlank(data.getId())) {
            throw new TokenServiceException("TokenData.id is null");
        }
    }


}

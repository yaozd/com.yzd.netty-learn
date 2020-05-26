package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.config.K8sConfig;
import com.yzd.netty.resolver.config.K8sTokenConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description: token管理
 */
public class K8sTokenProvider {
    private static volatile K8sTokenProvider mInstance;
    private Map<String, String> tokenMap = new HashMap<>();

    private K8sTokenProvider(K8sConfig k8sConfig) {
        load(k8sConfig);
    }

    public static K8sTokenProvider getInstance() {
        return mInstance;
    }

    public static K8sTokenProvider init(K8sConfig k8sConfig) {
        if (mInstance == null) {
            synchronized (K8sTokenProvider.class) {
                if (mInstance == null) {
                    mInstance = new K8sTokenProvider(k8sConfig);
                }
            }
        }
        return mInstance;
    }

    private void load(K8sConfig k8sConfig) {
        for (K8sTokenConfig token : k8sConfig.getTokens()) {
            tokenMap.put(token.host, readToken(token.tokenPath));
        }
    }

    public String getToken(String host) {
        return tokenMap.get(host);
    }

    private String readToken(String tokenPath) {
        File tokenFile = new File(tokenPath);
        List<String> lines = null;
        try {
            lines = Files.readAllLines(tokenFile.toPath(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (lines.size() < 1) {
            throw new RuntimeException("There is no valid token! token path:" + tokenPath);
        }
        String token = lines.get(0);
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("There is no valid token! token path:" + tokenPath);
        }
        return token.trim();
    }
}

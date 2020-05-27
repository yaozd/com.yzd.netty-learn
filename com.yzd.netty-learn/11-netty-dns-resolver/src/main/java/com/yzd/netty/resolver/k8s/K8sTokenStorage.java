package com.yzd.netty.resolver.k8s;

import com.yzd.netty.resolver.config.K8sConfig;
import com.yzd.netty.resolver.config.K8sTokenConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
@Slf4j
public class K8sTokenStorage {
    private static volatile K8sTokenStorage mInstance;
    private Map<String, String> tokenMap = new HashMap<>();

    private K8sTokenStorage(K8sConfig k8sConfig) {
        load(k8sConfig);
    }

    public static K8sTokenStorage getInstance() {
        return mInstance;
    }

    public static K8sTokenStorage init(K8sConfig k8sConfig) {
        if (mInstance == null) {
            synchronized (K8sTokenStorage.class) {
                if (mInstance == null) {
                    mInstance = new K8sTokenStorage(k8sConfig);
                }
            }
        }
        return mInstance;
    }

    private void load(K8sConfig k8sConfig) {
        for (K8sTokenConfig token : k8sConfig.getTokens()) {
            try {
                tokenMap.put(token.getHost(), readToken(token.getTokenPath()));
            } catch (Exception e) {
                String errorMsg = String.format("K8s token load fail! token config info(host:%s,tokenPath:%s),caused by :%s"
                        , token.getHost(), token.getTokenPath(), e.toString());
                throw new RuntimeException(errorMsg, e);
            }
            log.info("K8s token load success! token config info(host:{},tokenPath:{})",token.getHost(),token.getTokenPath());
        }
    }

    public String getToken(String host) {
        return tokenMap.get(host);
    }

    private String readToken(String tokenPath) throws IOException {
        if(StringUtils.isBlank(tokenPath)){
            throw new RuntimeException("tokenPath is blank!");
        }
        tokenPath = StringUtils.replace(tokenPath, "/", File.separator);
        tokenPath = StringUtils.replace(tokenPath, "\\", File.separator);
        File tokenFile = new File(tokenPath);
        List<String> lines = null;
        lines = Files.readAllLines(tokenFile.toPath(), Charset.forName("UTF-8"));
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

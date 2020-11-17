package com.yzd.common.urlExt;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * sq-grpc 封装了现有rpc框架grpc
 * https://github.com/fang-yan-peng/sq-grpc.git
 *
 * @Author: yaozh
 * @Description:
 */
public final class UriBuilder {
    private String protocol;

    private String username;

    private String password;

    private String host;

    private int port;

    private String path;

    private Map<String, String> parameters;

    public UriBuilder() {
        protocol = null;
        username = null;
        password = null;
        host = null;
        port = 0;
        path = null;
        parameters = new HashMap<>();
    }

    public UriBuilder(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public UriBuilder(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    public static UriBuilder from(URI url) {
        String protocol = url.getScheme();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        Map<String, String> parameters = new HashMap<>(UriUtil.parseQueryString(url.getQuery()));
        return new UriBuilder(
                protocol,
                null,
                null,
                host,
                port,
                path,
                parameters);
    }


    public UriBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public UriBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UriBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public UriBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public UriBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public UriBuilder setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        this.host = host;
        this.port = port;
        return this;
    }

    public UriBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public UriBuilder addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return this;
        }
        this.parameters.putAll(parameters);
        return this;
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }
}

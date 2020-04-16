package com.yzd.netty.opentracing.sender;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class NettyClient {

    public ClientStatus status = ClientStatus.INIT;
    public Channel channel;

    public enum ClientStatus {
        INIT,
        CONNECTING,
        SUCCESS,
        FAILURE;
    }
}

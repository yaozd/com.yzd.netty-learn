package com.yzd.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @Author: yaozh
 * @Description:
 */
public class Http2Util {
    /**
     * grpc ping response data
     *
     * @return
     */
    public static ByteBuf getGrpcPingResponseData() {
        byte[] value = String.valueOf(System.currentTimeMillis()).getBytes(UTF_8);
        ByteBuf buffer = Unpooled.buffer();
        // compressed flag 为0
        buffer.writeByte((byte) 0);
        // 设置message长度
        buffer.writeBytes(ByteBuffer.allocate(4).putInt(value.length + 2).array());
        // 设置field number和wire type
        buffer.writeByte((byte) 10);
        // 设置value长度
        buffer.writeByte((byte) value.length);
        buffer.writeBytes(value);
        return buffer;
    }

}

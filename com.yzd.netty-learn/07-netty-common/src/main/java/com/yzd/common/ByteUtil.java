package com.yzd.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @Author: yaozh
 * @Description:
 */
public class ByteUtil {
    @Test
    public void stringToByte() {
        String inputString = System.currentTimeMillis() + "";
        byte[] bytes = inputString.getBytes(UTF_8);
        byte[] data = new byte[bytes.length + 1];
        //
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000_000; i++) {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeByte(0);
            buffer.writeBytes(intToByte(bytes.length + 2));
            buffer.writeInt(1);
            byte[] bytes1 = ByteBufUtil.getBytes(buffer);
        }
        long end = System.currentTimeMillis();
        System.out.println("使用时间(ms): " + (end - start));
    }

    public byte[] intToByte(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * GrpcPing/Ping模拟包
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(getMessage("1599530473562")));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000_000; i++) {
            getMessage("" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println("使用时间(ms): " + (end - start));
    }

    /**
     * GrpcPing/Ping模拟包
     * 1.
     * Protobuf message:Compressed Flag+Message Length+Message Data(Data:Field Id(varint编码)+Value)
     * PS:
     * https://halfrost.com/protobuf_encode/
     * https://github.com/halfrost/Halfrost-Field/blob/master/contents/Protocol/Protocol-buffers-encode.md#%E5%85%AD-protocol-buffer-%E7%BC%96%E7%A0%81%E5%8E%9F%E7%90%86
     * 2.
     * Protobuf encoding types:UTF-8
     * PS: https://developers.google.com/protocol-buffers/docs/encoding#types
     * @param rawValue
     * @return
     * @throws IOException
     */
    private static byte[] getMessage(String rawValue) throws IOException {
        byte[] value = rawValue.getBytes(UTF_8);
        if (value.length > 127) { // 不支持长度超过127的消息
            return null;
        }
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write((byte) 0); // compressed flag 为0
        res.write(ByteBuffer.allocate(4).putInt(value.length + 2).array()); // 设置message长度
        res.write((byte) 10); // 设置field number和wire type
        res.write((byte) value.length); // 设置value长度
        res.write(value);
        return res.toByteArray();
    }
}

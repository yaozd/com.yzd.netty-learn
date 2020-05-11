package com.yzd.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCounted;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * @Author: yaozh
 * @Description:
 */
public class ByteBufferTest {
    // 创建简体中文对应的Charset
    Charset charset = Charset.forName("utf-8");
    // 获取charset对象对应的编码器
    CharsetDecoder charsetDecoder = charset.newDecoder();
    @Test
    public void byteBufferTest() throws CharacterCodingException {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeCharSequence((CharSequence) "te11111111111111111111111111111111111111111111111111111111111111111111111", charset);
        int capacity = buffer.capacity();
        System.out.println(capacity);
        ByteBuffer byteBuffer = buffer.nioBuffer();
        //byteBuffer.flip();
        // 将ByteBuffer的数据解码成字符序列
        System.out.println(charsetDecoder.decode(byteBuffer));
        int i = buffer.refCnt();
        System.out.println(i);
        // ByteBuffer内存释放
        if (buffer.refCnt() > 0) {
            buffer.release();
        }
    }
    public void releaseData(Object data) {
        if (data instanceof ReferenceCounted) {
            ReferenceCounted referenceCounted = (ReferenceCounted)data;
            if (referenceCounted.refCnt() > 0) {
                referenceCounted.release();
            }
        }
    }
}

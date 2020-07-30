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
            ReferenceCounted referenceCounted = (ReferenceCounted) data;
            if (referenceCounted.refCnt() > 0) {
                referenceCounted.release();
            }
        }
    }

    //参考：HyperspaceHttp2Codec
    private static final ByteBuf HTTP_1_X_BUF = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(
            new byte[]{'H', 'T', 'T', 'P', '/', '1', '.'})).asReadOnly();
    private static final ByteBuf CRLF = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(
            new byte[]{'\n', '\r'})).asReadOnly();
    private static final byte CR = 10;
    private static final byte LF = 13;

    /**
     * byteBuf解析
     * 判断是否以CRLF结尾
     * byteBuf to bytes：将数据从内核缓冲区拷贝到用户空间缓冲区,之后系统调用 read 返回,会降低性能
     */
    @Test
    public void byteBufferParseTest() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeCharSequence((CharSequence) "te11111111111111\n\r", charset);
        int i = buffer.readerIndex();
        System.out.println(i);
        int length = buffer.readableBytes();
        System.out.println(length);
        byte[] bytes = ByteBufUtil.getBytes(buffer);
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }
        System.err.println("判断是否以CRLF结尾");
        System.out.println("=========================");
        int b = ByteBufUtil.indexOf(CRLF, buffer.setIndex(i, length));
        System.out.println(b);
        System.out.println("T1:是否以CRLF结尾:" + (length - b == 2));
        System.out.println("=========================");
        //int crIndex = ByteBufUtil.indexOf(buffer.slice(length-2,1), 1, 1, CR);
        if (length >= 3) {
            int crIndex = ByteBufUtil.indexOf(buffer, length - 1, length - 2, CR);
            System.out.println(crIndex);
            int lfIndex = ByteBufUtil.indexOf(buffer, length, length - 1, LF);
            System.out.println(lfIndex);
            if (length - lfIndex == 1 && length - crIndex == 2) {
                System.out.println("T2:以CRLF结尾:TRUE");
            }
        }

        System.out.println("=========================");
        //byteBuf to bytes：将数据从内核缓冲区拷贝到用户空间缓冲区,之后系统调用 read 返回,会降低性能
        byte[] newBytes = ByteBufUtil.getBytes(buffer, length - 2, 2);
        for (byte newByte : newBytes) {
            System.out.println(newByte);
        }
        if (newBytes[0] == CR && newBytes[1] == LF) {
            System.out.println("T3:以CRLF结尾:TRUE");
        }
    }
}

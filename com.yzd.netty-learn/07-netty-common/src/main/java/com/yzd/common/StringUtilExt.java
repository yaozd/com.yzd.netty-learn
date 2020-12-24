package com.yzd.common;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * @Author: yaozh
 * @Description:
 */
public class StringUtilExt {

    public String convertByteBufToString1(ByteBuf buf){
        return buf.toString(0, buf.capacity(), CharsetUtil.US_ASCII);
    }
    /**
     * Netty ByteBuf 转 String
     * https://blog.csdn.net/o1101574955/article/details/81024102
     * @param buf
     * @return
     */
    public String convertByteBufToString2(ByteBuf buf) {
        String str;
        if(buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }

}

package com.yzd.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * 参考：
 * package io.netty.handler.codec.http.websocketx;
 * WebSocket08FrameDecoder
 * 参考：
 * websocket 解码器-代码详解
 * - [Netty源码分析-Websocket之WebSocket08FrameDecoder](https://blog.csdn.net/nimasike/article/details/99230805)
 *
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class WebSocketFrameUtil {
    //定义opcode
    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    //STATE
    private static final int READING_FIRST = 1;
    private static final int READING_SECOND = 2;
    private static final int READING_SIZE = 3;
    private static final int MASKING_KEY = 4;
    private static final int PAYLOAD = 5;

    //
    private static final int maxFramePayloadLength = Integer.MAX_VALUE;

    /**
     * TODO  data.duplicate()复制的对象与原来的ByteBuf是共离缓存，不需要再手动释放对象
     *
     * @param data
     */
    public static WebSocketFrame decode(ByteBuf data) {
        try {
            return convertWebsocketFrame(data);
        } catch (Throwable throwable) {
            log.error("Websocket decode failed!", throwable);
            return null;
        }
    }

    private static WebSocketFrame convertWebsocketFrame(ByteBuf data) {
        ByteBuf in = data.duplicate();
        int state = READING_FIRST;
        long framePayloadLength = 0L;
        boolean frameFinalFlag = false;
        int frameRsv = 0;
        int frameOpcode = 0;
        boolean frameMasked = false;
        int framePayloadLen1 = 0;
        byte[] maskingKey = null;
        switch (state) {
            case READING_FIRST:
                if (!in.isReadable()) {
                    return null;
                }
                //把荷载数据长度设置为0
                //int framePayloadLength = 0;
                // FIN, RSV, OPCODE
                //读取ws帧的第一个字节，解析出FIN  RSV OPCODE
                byte b = in.readByte();
                //b & 10000000 得到FIN
                frameFinalFlag = (b & 0x80) != 0;
                //b & 01110000 完了右移4位 得到RSV
                frameRsv = (b & 0x70) >> 4;
                // b & 00001111 得到opcode
                frameOpcode = b & 0x0F;
                log.info("frameFinalFlag[{}],frameRsv[{}],frameOpcode[{}]", frameFinalFlag, frameRsv, frameOpcode);
                state = READING_FIRST;
            case READING_SECOND:
                if (!in.isReadable()) {
                    return null;
                }
                // MASK, PAYLOAD LEN 1
                b = in.readByte();
                frameMasked = (b & 0x80) != 0;
                framePayloadLen1 = b & 0x7F;

                if (frameRsv != 0) {
                    protocolViolation("RSV != 0 and no extension negotiated, RSV:" + frameRsv);
                    return null;
                }
                // control frame (have MSB in opcode set)
                if (frameOpcode > 7) {

                    // control frames MUST NOT be fragmented
                    if (!frameFinalFlag) {
                        protocolViolation("fragmented control frame,not be finish frame");
                        return null;
                    }

                    // control frames MUST have payload 125 octets or less
                    if (framePayloadLen1 > 125) {
                        protocolViolation("control frame with payload length > 125 octets");
                        return null;
                    }
                    // check for reserved control frame opcodes
                    if (!(frameOpcode == OPCODE_CLOSE || frameOpcode == OPCODE_PING
                            || frameOpcode == OPCODE_PONG)) {
                        protocolViolation("control frame using reserved opcode " + frameOpcode);
                        return null;
                    }

                    // close frame : if there is a body, the first two bytes of the
                    // body MUST be a 2-byte unsigned integer (in network byte
                    // order) representing a getStatus code
                    if (frameOpcode == 8 && framePayloadLen1 == 1) {
                        protocolViolation("received close control frame with payload len 1");
                        return null;
                    }
                } else { // data frame
                    // check for reserved data frame opcodes
                    if (!(frameOpcode == OPCODE_CONT || frameOpcode == OPCODE_TEXT
                            || frameOpcode == OPCODE_BINARY)) {
                        protocolViolation("data frame using reserved opcode " + frameOpcode);
                        return null;
                    }
                }
                state = READING_SIZE;
            case READING_SIZE:

                // Read frame payload length
                if (framePayloadLen1 == 126) {
                    if (in.readableBytes() < 2) {
                        return null;
                    }
                    framePayloadLength = in.readUnsignedShort();
                    if (framePayloadLength < 126) {
                        protocolViolation("invalid data frame length (not using minimal length encoding)");
                        return null;
                    }
                } else if (framePayloadLen1 == 127) {
                    if (in.readableBytes() < 8) {
                        return null;
                    }
                    framePayloadLength = in.readLong();
                    if (framePayloadLength < 65536) {
                        protocolViolation("invalid data frame length (not using minimal length encoding)");
                        return null;
                    }
                } else {
                    framePayloadLength = framePayloadLen1;
                }

                if (framePayloadLength > maxFramePayloadLength) {
                    protocolViolation("Max frame length of " + maxFramePayloadLength + " has been exceeded.");
                    return null;
                }
                state = MASKING_KEY;
                log.info("frameMasked:[{}]", frameMasked);
                //return;
            case MASKING_KEY:
                if (frameMasked) {
                    if (in.readableBytes() < 4) {
                        return null;
                    }
                    if (maskingKey == null) {
                        maskingKey = new byte[4];
                    }
                    in.readBytes(maskingKey);
                }
                state = PAYLOAD;
                log.info("PAYLOAD");
            case PAYLOAD:
                if (in.readableBytes() < framePayloadLength) {
                    return null;
                }
                if (frameOpcode == OPCODE_TEXT) {
                    String text = getText(in, frameMasked, maskingKey);
                    log.info(text);
                    return new TextWebSocketFrame(text == null ? "" : text);
                }
                if (frameOpcode == OPCODE_PING) {
                    log.info("PingWebSocketFrame");
                    return new PingWebSocketFrame();
                }
                if (frameOpcode == OPCODE_PONG) {
                    log.info("PongWebSocketFrame");
                    return new PongWebSocketFrame();
                }
            default:
                return null;
        }
    }

    private static int toFrameLength(long l) {
        if (l > Integer.MAX_VALUE) {
            throw new TooLongFrameException("Length:" + l);
        } else {
            return (int) l;
        }
    }

    private static String getText(ByteBuf in, boolean frameMasked, byte[] maskingKey) {
        if (frameMasked) {
            return unmask(maskingKey, in);
        }
        return byteBufToString(in);
    }

    private static String byteBufToString(ByteBuf buf) {
        String str;
        //处理堆缓冲区
        if (buf.hasArray()) {
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }

    /**
     * 掩码解码
     *
     * @param maskingKey
     * @param frame
     * @return
     */
    private static String unmask(byte[] maskingKey, ByteBuf frame) {
        byte[] maskedBytes = ByteBufUtil.getBytes(frame);
        int length = maskedBytes.length;
        //解码的结果
        byte[] unmaskedByte = new byte[length];
        for (int i = 0; i < length; ++i) {
            byte masking = maskingKey[i % 4];
            //XOR运算，还原原始值
            unmaskedByte[i] = (byte) (maskedBytes[i] ^ masking);
        }
        return new String(unmaskedByte);
    }

    private static void protocolViolation(String message) {
        log.error(message);
    }
}

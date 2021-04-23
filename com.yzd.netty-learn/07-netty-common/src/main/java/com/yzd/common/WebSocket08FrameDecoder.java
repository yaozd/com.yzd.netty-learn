//package com.yzd.common;
//
///**
// * @Author: yaozh
// * @Description:
// */
//
//public class WebSocket08FrameDecoder extends ByteToMessageDecoder
//        implements WebSocketFrameDecoder {
//
//    //当前解码器状态枚举
//    enum State {
//        READING_FIRST,
//        READING_SECOND,
//        READING_SIZE,
//        MASKING_KEY,
//        PAYLOAD,
//        CORRUPT
//    }
//
//    //定义opcode
//    private static final byte OPCODE_CONT = 0x0;
//    private static final byte OPCODE_TEXT = 0x1;
//    private static final byte OPCODE_BINARY = 0x2;
//    private static final byte OPCODE_CLOSE = 0x8;
//    private static final byte OPCODE_PING = 0x9;
//    private static final byte OPCODE_PONG = 0xA;
//
//    //Websocket最大荷载数据长度，超过该值抛出异常
//    private final long maxFramePayloadLength;
//
//    //是否允许WS扩展
//    private final boolean allowExtensions;
//
//    //是否期望对荷载数据进行掩码-客户端发送的数据必须要掩码
//    private final boolean expectMaskedFrames;
//
//    //是否允许掩码缺失
//    private final boolean allowMaskMismatch;
//
//    //分片发送的数量
//    private int fragmentedFramesCount;
//
//    //当前ws帧是否是完整的
//    private boolean frameFinalFlag;
//
//    //当前ws荷载数据是否已经掩码
//    private boolean frameMasked;
//
//    //RSV1 RSV2 RSV3
//    private int frameRsv;
//
//    //ws帧内 opocde的值
//    private int frameOpcode;
//
//    //荷载数据的长度
//    private long framePayloadLength;
//    //掩码
//    private byte[] maskingKey;
//    //ws协议PayloadLength表示的长度
//    private int framePayloadLen1;
//
//    //是否收到关闭帧
//    private boolean receivedClosingHandshake;
//
//    //初始状态
//    private State state = State.READING_FIRST;
//
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//
//        // Discard all data received if closing handshake was received before.
//        //如果已经收到关闭帧，则丢弃说有字节
//        if (receivedClosingHandshake) {
//            in.skipBytes(actualReadableBytes());
//            return;
//        }
//        switch (state) {
//            case READING_FIRST:
//                if (!in.isReadable()) {
//                    return;
//                }
//
//                //把荷载数据长度设置为0
//                framePayloadLength = 0;
//
//                // FIN, RSV, OPCODE
//                //读取ws帧的第一个字节，解析出FIN  RSV OPCODE
//                byte b = in.readByte();
//                frameFinalFlag = (b & 0x80) != 0;  //b & 10000000 得到FIN
//                frameRsv = (b & 0x70) >> 4;        //b & 01110000 完了右移4位 得到RSV
//                frameOpcode = b & 0x0F;  // b & 00001111 得到opcode
//
//                //改变状态
//                state = State.READING_SECOND;
//            case READING_SECOND:
//                if (!in.isReadable()) {
//                    return;
//                }
//                //读取ws帧的第二个字节
//                // MASK, PAYLOAD LEN 1
//                b = in.readByte();
//
//                //计算是否需要掩码
//                frameMasked = (b & 0x80) != 0;
//                //ws协议PayloadLength表示的长度
//                framePayloadLen1 = b & 0x7F;
//
//                //如果RSV不为0说明使用了WS扩展协议，allowExtensions如果设置为不允许扩展则报错
//                //目前RSV都为0，还没有扩展协议
//                if (frameRsv != 0 && !allowExtensions) {
//                    protocolViolation(ctx, "RSV != 0 and no extension negotiated, RSV:" + frameRsv);
//                    return;
//                }
//
//                //如果不允许缺失掩码  并且 客户端又没有掩码 则报错
//                if (!allowMaskMismatch && expectMaskedFrames != frameMasked) {
//                    protocolViolation(ctx, "received a frame that is not masked as expected");
//                    return;
//                }
//
//                //如果opcpde为一个控制帧 如果 ping pong close
//                if (frameOpcode > 7) { // control frame (have MSB in opcode set)
//
//                    // control frames MUST NOT be fragmented
//                    //控制帧必须是一个完整的帧，所有frameFinalFlag必须为true
//                    if (!frameFinalFlag) {
//                        protocolViolation(ctx, "fragmented control frame");
//                        return;
//                    }
//
//                    //控制帧framePayload必须小于等于125
//                    // control frames MUST have payload 125 octets or less
//                    if (framePayloadLen1 > 125) {
//                        protocolViolation(ctx, "control frame with payload length > 125 octets");
//                        return;
//                    }
//
//                    //控制帧目前只能是close ping pong，其它目前ws还未定义，出现则报错
//                    // check for reserved control frame opcodes
//                    if (!(frameOpcode == OPCODE_CLOSE || frameOpcode == OPCODE_PING
//                            || frameOpcode == OPCODE_PONG)) {
//                        protocolViolation(ctx, "control frame using reserved opcode " + frameOpcode);
//                        return;
//                    }
//
//                    // close frame : if there is a body, the first two bytes of the
//                    // body MUST be a 2-byte unsigned integer (in network byte
//                    // order) representing a getStatus code
//                    //关闭帧framePayloadLen1必为0，不能携带数据
//                    if (frameOpcode == 8 && framePayloadLen1 == 1) {
//                        protocolViolation(ctx, "received close control frame with payload len 1");
//                        return;
//                    }
//                } else { // data frame
//                    //小于7的都是数据帧
//                    //%x0：表示一个延续帧。当Opcode为0时，表示本次数据传输采用了数据分片，当前收到的数据帧为其中一个数据分片。
//                    //%x1：表示这是一个文本帧（frame）
//                    //%x2：表示这是一个二进制帧（frame）
//                    // check for reserved data frame opcodes
//                    //目前只支持这三种帧，其它抛出异常
//                    if (!(frameOpcode == OPCODE_CONT || frameOpcode == OPCODE_TEXT
//                            || frameOpcode == OPCODE_BINARY)) {
//                        protocolViolation(ctx, "data frame using reserved opcode " + frameOpcode);
//                        return;
//                    }
//
//                    //如果是延续帧，那前面必须有一个Text或Binary帧，通过fragmentedFramesCount>0来判断
//                    // check opcode vs message fragmentation state 1/2
//                    if (fragmentedFramesCount == 0 && frameOpcode == OPCODE_CONT) {
//                        protocolViolation(ctx, "received continuation data frame outside fragmented message");
//                        return;
//                    }
//
//                    //如果fragmentedFramesCount != 0 说明前面出现了text或binary帧，并且fin为false 指示后续还有数据
//                    //但是frameOpcode又不是一个延续帧，说明出现混乱情况报错
//                    //我觉得frameOpcode != OPCODE_PING是一个无效的判断
//                    // check opcode vs message fragmentation state 2/2
//                    if (fragmentedFramesCount != 0 && frameOpcode != OPCODE_CONT && frameOpcode != OPCODE_PING) {
//                        protocolViolation(ctx,
//                                "received non-continuation data frame while inside fragmented message");
//                        return;
//                    }
//                }
//
//                //修改状态
//                state = State.READING_SIZE;
//            case READING_SIZE:
//
//                // Read frame payload length
//                //如果payload length=126 后续2个字节是荷载数据的长度
//                if (framePayloadLen1 == 126) {
//                    if (in.readableBytes() < 2) {
//                        return;
//                    }
//                    //读2个字节，按无符号处理
//                    framePayloadLength = in.readUnsignedShort();
//                    if (framePayloadLength < 126) {
//                        protocolViolation(ctx, "invalid data frame length (not using minimal length encoding)");
//                        return;
//                    }
//
//                    //127 后续8个字节是何在数据的长度
//                } else if (framePayloadLen1 == 127) {
//                    if (in.readableBytes() < 8) {
//                        return;
//                    }
//                    //读取8个字节为数据长度
//                    framePayloadLength = in.readLong();
//                    // TODO: check if it's bigger than 0x7FFFFFFFFFFFFFFF, Maybe
//                    // just check if it's negative?
//
//                    if (framePayloadLength < 65536) {
//                        protocolViolation(ctx, "invalid data frame length (not using minimal length encoding)");
//                        return;
//                    }
//                } else {
//                    //payload length<125 说明framePayloadLen1本身就表示数据长度
//                    framePayloadLength = framePayloadLen1;
//                }
//
//                //如果荷载数据的长度 大于阈值，抛出异常
//                if (framePayloadLength > maxFramePayloadLength) {
//                    protocolViolation(ctx, "Max frame length of " + maxFramePayloadLength + " has been exceeded.");
//                    return;
//                }
//
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Decoding WebSocket Frame length={}", framePayloadLength);
//                }
//
//                //转换状态
//                state = State.MASKING_KEY;
//            case MASKING_KEY:
//                //是否有掩码
//                if (frameMasked) {
//                    if (in.readableBytes() < 4) {
//                        return;
//                    }
//
//                    //读取4个字节，读取掩码
//                    if (maskingKey == null) {
//                        maskingKey = new byte[4];
//                    }
//                    in.readBytes(maskingKey);
//                }
//
//                //转换状态
//                state = State.PAYLOAD;
//            case PAYLOAD:
//                //可读数据达不到荷载数据长度则等待下一轮事件
//                if (in.readableBytes() < framePayloadLength) {
//                    return;
//                }
//
//                ByteBuf payloadBuffer = null;
//                try {
//                    //将荷载数据读到新的缓冲区中
//                    payloadBuffer = readBytes(ctx.alloc(), in, toFrameLength(framePayloadLength));
//
//                    //切换状态为初始状态，进行下一轮读取。
//                    state = State.READING_FIRST;
//
//                    //如果有掩码，需要进行XOR二次计算还原出原文
//                    // Unmask data if needed
//                    if (frameMasked) {
//                        unmask(payloadBuffer);
//                    }
//
//                    // Processing ping/pong/close frames because they cannot be
//                    // fragmented
//                    //根据情况封装不同数据帧
//                    if (frameOpcode == OPCODE_PING) {
//                        out.add(new PingWebSocketFrame(frameFinalFlag, frameRsv, payloadBuffer));
//                        payloadBuffer = null;
//                        return;
//                    }
//                    if (frameOpcode == OPCODE_PONG) {
//                        out.add(new PongWebSocketFrame(frameFinalFlag, frameRsv, payloadBuffer));
//                        payloadBuffer = null;
//                        return;
//                    }
//                    if (frameOpcode == OPCODE_CLOSE) {
//                        //如果是对方发的Close帧则关闭socket
//                        receivedClosingHandshake = true;
//                        checkCloseFrameBody(ctx, payloadBuffer);
//                        out.add(new CloseWebSocketFrame(frameFinalFlag, frameRsv, payloadBuffer));
//                        payloadBuffer = null;
//                        return;
//                    }
//
//                    // Processing for possible fragmented messages for text and binary
//                    // frames
//                    if (frameFinalFlag) {
//                        //如果是最终的分片则fragmentedFramesCount=0
//                        // Final frame of the sequence. Apparently ping frames are
//                        // allowed in the middle of a fragmented message
//                        if (frameOpcode != OPCODE_PING) {
//                            fragmentedFramesCount = 0;
//                        }
//                    } else {
//                        // Increment counter
//                        //否则fragmentedFramesCount++
//                        fragmentedFramesCount++;
//                    }
//
//                    // 返回各种帧
//                    if (frameOpcode == OPCODE_TEXT) {
//                        out.add(new TextWebSocketFrame(frameFinalFlag, frameRsv, payloadBuffer));
//                        payloadBuffer = null;
//                        return;
//                    } else if (frameOpcode == OPCODE_BINARY) {
//                        out.add(new BinaryWebSocketFrame(frameFinalFlag, frameRsv, payloadBuffer));
//                        payloadBuffer = null;
//                        return;
//                    } else if (frameOpcode == OPCODE_CONT) {
//                        out.add(new ContinuationWebSocketFrame(frameFinalFlag, frameRsv,
//                                payloadBuffer));
//                        payloadBuffer = null;
//                        return;
//                    } else {
//                        throw new UnsupportedOperationException("Cannot decode web socket frame with opcode: "
//                                + frameOpcode);
//                    }
//                } finally {
//                    //释放缓冲区，如果payloadBuffer！=null 说明没有成功返回数据帧
//                    if (payloadBuffer != null) {
//                        payloadBuffer.release();
//                    }
//                }
//            case CORRUPT:
//                if (in.isReadable()) {
//                    // If we don't keep reading Netty will throw an exception saying
//                    // we can't return null if no bytes read and state not changed.
//                    in.readByte();
//                }
//                return;
//            default:
//                throw new Error("Shouldn't reach here.");
//        }
//    }
//
//    private void unmask(ByteBuf frame) {
//        int i = frame.readerIndex();
//        int end = frame.writerIndex();
//
//        ByteOrder order = frame.order();
//
//        //把掩码二进制数组转换为int
//        int intMask = ((maskingKey[0] & 0xFF) << 24)
//                | ((maskingKey[1] & 0xFF) << 16)
//                | ((maskingKey[2] & 0xFF) << 8)
//                | (maskingKey[3] & 0xFF);
//
//        //如果是小端序，需要把INT类型的掩码反转
//        if (order == ByteOrder.LITTLE_ENDIAN) {
//            intMask = Integer.reverseBytes(intMask);
//        }
//
//        //XOR运算，还原原始值
//        for (; i + 3 < end; i += 4) {
//            int unmasked = frame.getInt(i) ^ intMask;
//            frame.setInt(i, unmasked);
//        }
//        for (; i < end; i++) {
//            frame.setByte(i, frame.getByte(i) ^ maskingKey[i % 4]);
//        }
//    }
//
//    //抛出异常
//    private void protocolViolation(ChannelHandlerContext ctx, String reason) {
//        protocolViolation(ctx, new CorruptedFrameException(reason));
//    }
//
//    //抛出异常，关闭socket
//    private void protocolViolation(ChannelHandlerContext ctx, CorruptedFrameException ex) {
//        state = State.CORRUPT;
//        if (ctx.channel().isActive()) {
//            Object closeMessage;
//            if (receivedClosingHandshake) {
//                closeMessage = Unpooled.EMPTY_BUFFER;
//            } else {
//                closeMessage = new CloseWebSocketFrame(1002, null);
//            }
//            ctx.writeAndFlush(closeMessage).addListener(ChannelFutureListener.CLOSE);
//        }
//        throw ex;
//    }
//}
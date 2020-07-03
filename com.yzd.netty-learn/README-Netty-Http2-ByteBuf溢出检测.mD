## Netty-Http2-ByteBuf溢出检测
```
    AtomicInteger j = new AtomicInteger();
    //检测bytebuf是否有溢出的可能性
    @Override
    public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        ChannelOutboundBuffer channelOutboundBuffer = ctx.channel().unsafe().outboundBuffer();
        if (channelOutboundBuffer != null) {
            for (; ; ) {
                if (!channelOutboundBuffer.remove()) {
                    break;
                }
            }
        }
        for (ByteBuf byteBuf : inList) {
            //log.error("T1:byteBuf.refCnt()-VALUE:"+byteBuf.refCnt());
            //检测bytebuf是否有溢出的可能性
            if (byteBuf.refCnt() > 0) {
                int i = byteBuf.refCnt();
                String s = hexDump(byteBuf);
                boolean readable = byteBuf.isReadable();
                System.out.println("SSSSSSSSSS:" + s);
                log.error("T2:byteBuf.refCnt()>0:" + byteBuf.refCnt() + ";inList.size()" + inList.size() + ";j=" + j.incrementAndGet() + ";channelId:" + ctx.channel().id() + ";internalBuffer:" + byteBuf.toString() + ";hexDump" + ByteBufUtil.hexDump(byteBuf) + readable);
                //NettyReleaseUtil.completelyReleaseData(byteBuf);
            }
        }
    }

    //收集当前通道中所有bytebuf数据，用于检测bytebuf是否有溢出的可能性
    List<ByteBuf> inList = new LinkedList<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        inList.add(in);
//        if (!in.isReadable()) {
//            in.release();
//            return;
//        }
        try {
            if (isPrefaceDecoded) {
                frameReader.readFrame(ctx, in, readFrameListener);
                return;
            }
            if (readClientPrefaceString(in) && verifyFirstFrameIsSettings(in)) {
                isPrefaceDecoded = true;
                frameReader.readFrame(ctx, in, readFrameListener);
                return;
            }
            log.error("T5:" + in.refCnt());
        } catch (Exception e) {
            log.error("Decode http2 message failed!", e);
            ctx.fireExceptionCaught(e);
        }finally {
            //如果当前的bytebuf中已经没有可读数据时，则in.refCnt应该等于1
            //in.refCnt()>1情况发生在多个客服中同并行发送大量请求中，客户端突然关闭的情况下，可能会产生。
            if (!in.isReadable()) {
                if(in.refCnt()>1){
                    log.error("=========in.refCnt()>1========="+in.refCnt());
                    in.release();
                }
            }
        }
    }
```
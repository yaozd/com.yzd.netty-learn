## sharable 使用场景：
> 如果你的 Handler 是无状态的话, 则可以将它声明为 @Sharable 的, 然后只创建一次实例即可.而不用每次 Channel 初始化时, 都 new 一个.
```
@sharable注解表明这个handler可以在多线程环境下使用， 当你自己写了一个非线程安全的handler， 不要去sharable， 否则自毁长城）
应该是一个关于连接无状态的，比如，对于拆包沾包handler，每个连接都应该有一个自己的对象，故不能在各连接复用一个这样的Sharable的handler
场景：     
1. 通常无状态的handler都是可以用这个标记的， 那么只需要存在一个handler instance， 减少资源开销      
2. 如你想对global的一些数据搞点事情， 那么就可以用这个了， 比如对ip的限制-UniqueIpFilter
3. 如果你需要全局统计一些信息，比如所有连接报错次数（exceptionCaught）等 PS:所有连接,则是无状态的
```
- 以下是所有 Netty 包下, Sharable 的handler:
```
sharable class size =>  34
sharable class => io.netty.handler.codec.dns.DatagramDnsQueryEncoder
sharable class => io.netty.handler.codec.socksx.v4.Socks4ServerEncoder
sharable class => io.netty.handler.codec.string.LineEncoder
sharable class => io.netty.handler.traffic.GlobalTrafficShapingHandler
sharable class => io.netty.handler.codec.LengthFieldPrepender
sharable class => io.netty.handler.codec.dns.DatagramDnsQueryDecoder
sharable class => io.netty.handler.codec.protobuf.ProtobufDecoderNano
sharable class => io.netty.handler.codec.base64.Base64Encoder
sharable class => io.netty.handler.codec.string.StringEncoder
sharable class => io.netty.handler.ipfilter.RuleBasedIpFilter
sharable class => io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
sharable class => io.netty.handler.codec.rtsp.RtspObjectEncoder
sharable class => io.netty.handler.codec.socks.SocksMessageEncoder
sharable class => io.netty.handler.codec.bytes.ByteArrayEncoder
sharable class => io.netty.handler.codec.serialization.ObjectEncoder
sharable class => io.netty.handler.ipfilter.UniqueIpFilter
sharable class => io.netty.handler.codec.socksx.v5.Socks5ClientEncoder
sharable class => io.netty.handler.codec.protobuf.ProtobufEncoder
sharable class => io.netty.handler.codec.socksx.v4.Socks4ClientEncoder
sharable class => io.netty.handler.codec.dns.DatagramDnsResponseEncoder
sharable class => io.netty.handler.logging.LoggingHandler
sharable class => io.netty.handler.codec.mqtt.MqttEncoder
sharable class => io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
sharable class => io.netty.handler.traffic.GlobalChannelTrafficShapingHandler
sharable class => io.netty.handler.codec.string.StringDecoder
sharable class => io.netty.handler.codec.marshalling.MarshallingEncoder
sharable class => io.netty.handler.codec.protobuf.ProtobufDecoder
sharable class => io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec
sharable class => io.netty.handler.codec.dns.DatagramDnsResponseDecoder
sharable class => io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
sharable class => io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder
sharable class => io.netty.handler.codec.base64.Base64Decoder
sharable class => io.netty.handler.codec.protobuf.ProtobufEncoderNano
sharable class => io.netty.handler.codec.marshalling.CompatibleMarshallingEncoder
```

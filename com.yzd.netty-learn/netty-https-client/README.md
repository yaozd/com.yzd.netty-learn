## SSL-Netty
- [Netty SSL双向验证](http://www.easysb.cn/2019/07/492.html) -次要参考byArvin
- [Netty实现SSL双向验证完整实例](https://blog.csdn.net/moonpure/article/details/82863181)
- [netty4 HTTPclient 可添加参数](https://www.cnblogs.com/justeene/p/4045567.html)- 首要参考byArvin

## 示例
- 服务端的代码
```
@Slf4j
public class HidsSslContextBuilder {
    private final static String serverCrt = "/static/keys/server.crt";
    private final static String serverKey = "/static/keys/pkcs8_server.key";
    private final static String caCrt = "/static/keys/ca.crt";
    private final static String keyPassword = "";


    public static SslContext build(ClientAuth clientAuth) {
        InputStream certInput = null;
        InputStream priKeyInput = null;
        InputStream caInput = null;
        try {
            certInput = HidsSslContextBuilder.class.getResourceAsStream(serverCrt);
            priKeyInput = HidsSslContextBuilder.class.getResourceAsStream(serverKey);
            caInput = HidsSslContextBuilder.class.getResourceAsStream(caCrt);
            return SslContextBuilder.forServer(certInput, priKeyInput)
                    .clientAuth(clientAuth)
                    .trustManager(caInput).build();
        } catch (Throwable e) {
            log.error("HidsSslContextBuilder", e);
        } finally {
            IOUtils.closeQuietly(certInput);
            IOUtils.closeQuietly(priKeyInput);
            IOUtils.closeQuietly(caInput);
        }
        return null;
    }


    public static SslContext buildSelfSignedCer() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .build();
        } catch (Throwable e) {
            log.error("buildSelfSignedCer", e);
        }
        return null;
    }
}

...
@Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // INBOUND： from head to tail
        // OUTBOUND: from tail to head
        // ssl
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
        }

        pipeline.addLast("TLVDecoder", new TLVDecoder());
        pipeline.addLast("TLVEncoder", new TLVEncoder());
        pipeline.addLast("Decompressor", new Decompressor());
        pipeline.addLast("Compressor", new Compressor());
        pipeline.addLast("tlvChannelHandler", tlvChannelHandler);

        // 增加channel对应的数据
        prepareChannelContext(socketChannel);
    }
```
- 客户端的测试代码
```
public class ClientChannelTest {

    @Test
    public void testClient() throws Throwable {
        String host = "0.0.0.0";
        int port = 8888;

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final SslContext sslCtx = SslContextBuilder.forClient()
                    // 双向验证
//                    .keyManager(this.getClass().getResourceAsStream("/keys/client.crt"),
//                            this.getClass().getResourceAsStream("/keys/pkcs8_client.key"))

                    // CA证书，验证对方证书
                    .trustManager(this.getClass().getResourceAsStream("/keys/ca.crt"))

                    // 不验证SERVER
                    // .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
                    ch.pipeline().addLast(new TLVDecoder());
                    ch.pipeline().addLast(new TLVEncoder());
                    ch.pipeline().addLast("Decompressor", new Decompressor());
                    ch.pipeline().addLast("Compressor", new Compressor());
                    ch.pipeline().addLast(new ClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
```
- 在Channel上绑定链接上下文信息，类似Session的功能，存储在Channel中的Attr中。
```
public class ChannelUtils {
    public static ClientContext getOrCreate(Channel socketChannel) {
        AttributeKey<ClientContext> key = AttributeKey.valueOf(ClientContext.class.toString());
        Attribute<ClientContext> attr = socketChannel.attr(key);
        if (attr.get() == null) {
            attr.set(createClientContext(socketChannel));
        }
        return attr.get();
    }

    private static ClientContext createClientContext(Channel socketChannel) {
        return new DefaultClientContext(socketChannel.pipeline());
    }
}
```

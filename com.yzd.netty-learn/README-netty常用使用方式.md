## netty常用使用方式
- [netty常用使用方式](https://www.cnblogs.com/fairjm/p/netty_common_pattern.html) -首要参考byArvin
### 统一模板
- 服务端启动模板（也可以不区分boss和worker 用一个）
```
public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new MyChannelInitializer());
            ChannelFuture future = serverBootstrap.bind(8999).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
}
```
- 客户端启动模板：
```
EventLoopGroup group = new NioEventLoopGroup();
try {
    Bootstrap bootstrap = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .handler(new MyChannelInitializer());
    ChannelFuture future = bootstrap.connect("localhost", 8888).sync();
    future.channel().closeFuture().sync();
} finally {
    group.shutdownGracefully();
}
```
- ChannelInitializer模板（继承ChannelInitializer即可）：
```
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(...);
    }

}
```
- 处理Http请求
```
netty自带了对用的codec类比较方便。
pipeline.addLast("httpServerCodec", new HttpServerCodec());
自己实现的handler最简单的方式用SimpleChannelInboundHandler接收HttpRequest方法即可
class MyHttpHandler extends SimpleChannelInboundHandler<HttpRequest>
这里简单说下SimpleChannelInboundHandler这个类，他是简化处理接受信息并处理的一个类，主要做两件事。

第一件事是根据泛型决定是否处理这个消息，能够处理就自己处理，不行就交给下一个（可以参考acceptInboundMessage和channelRead方法）。
第二件事是消息的自动回收（有构造函数支持 默认是true），消息的引用计数会减一（所以在其他地方还要使用记得再retain一下）。
使用它可以节省很多冗余代码的编写。
一个简单例子：

public class MyHttpHandler extends SimpleChannelInboundHandler<HttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        System.out.println(msg.getClass());
        System.out.println(msg.uri());
        System.out.println(msg.method().name());
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("headers:");
        msg.headers().forEach(System.out::println);
        ByteBuf buf = Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        ctx.writeAndFlush(response);
//         ctx.channel().close();
    }
}
不过只用这个handler并不能拿到content,还需要配合ChunkedWriteHandler和HttpObjectAggregator得到FullHttpRequest对象
```
- 处理WebSocket请求
```
只需要在上面的基础上增加一个WebSocketServerProtocolHandler即可，完整如下：

    pipeline.addLast("httpServerCodec", new HttpServerCodec());
    pipeline.addLast(new ChunkedWriteHandler());
    pipeline.addLast(new HttpObjectAggregator(8096));
    pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
自己的处理器可以接收并处理WebSocketFrame的子类。
```
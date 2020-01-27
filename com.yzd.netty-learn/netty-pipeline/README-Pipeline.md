## pipeline
- [netty中的ChannelHandler和ChannelPipeline](https://msd.misuland.com/pd/3181438578597036916)
- [Netty 的 Channel、Promise、Pipeline 详解](https://cloud.tencent.com/developer/article/1536343)
- [netty ChannelPipeline的事件传输机制](http://www.imooc.com/article/272419)
- 事件是如何在 Pipeline 中传递的
    ```
    比如下面的例子，以 Inbound 开头的类表示是入站处理程序，以 Outbound 开头的类表示是出站处理程序。
     ChannelPipeline p = ...;
     p.addLast("1", new InboundHandlerA());
     p.addLast("2", new InboundHandlerB());
     p.addLast("3", new OutboundHandlerA());
     p.addLast("4", new OutboundHandlerB());
     p.addLast("5", new InboundOutboundHandlerX());
    上面的示例配置中，事件进入时处理顺序是1，2，3，4，5；事件出站顺序为5，4，3，2，1。

    3 和 4 没有实现 ChannelInboundHandler，因此入站事件实际顺序是 1，2，5
    1 和 2 没有实现 ChannelOutboundHandler，因此出站事件实际顺序是 5，4，3
    5 同时实现了 ChannelInboundHandler 和 ChannelOutboundHandler
    将事件转发到下一个 Handler
    处理程序必须调用 ChannelHandlerContext 中的事件传播方法，将事件转发到其下一个处理程序。这些方法包括：

     入站事件传播方法：
    
    ChannelHandlerContext.fireChannelRegistered()
    ChannelHandlerContext.fireChannelActive()
    ChannelHandlerContext.fireChannelRead(Object)
    ChannelHandlerContext.fireChannelReadComplete()
    ChannelHandlerContext.fireExceptionCaught(Throwable)
    ChannelHandlerContext.fireUserEventTriggered(Object)
    ChannelHandlerContext.fireChannelWritabilityChanged()
    ChannelHandlerContext.fireChannelInactive()
    ChannelHandlerContext.fireChannelUnregistered()
     出站事件传播方法：
    
    ChannelOutboundInvoker.bind(SocketAddress, ChannelPromise)
    ChannelOutboundInvoker.connect(SocketAddress, SocketAddress, ChannelPromise)
    ChannelOutboundInvoker.write(Object, ChannelPromise)
    ChannelHandlerContext.flush()
    ChannelHandlerContext.read()
    ChannelOutboundInvoker.disconnect(ChannelPromise)
    ChannelOutboundInvoker.close(ChannelPromise)
    ChannelOutboundInvoker.deregister(ChannelPromise)
    下面的示例说明了事件是如何传播的：
    
    public class MyInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connected!");
        ctx.fireChannelActive();
        }
    }
    
    public class MyOutboundHandler extends ChannelOutboundHandlerAdapter {
    
        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
            System.out.println("Closing ..");
            ctx.close(promise);
        }
    }
    ```
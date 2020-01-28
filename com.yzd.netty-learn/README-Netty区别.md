- [Netty4中 Ctx.close 与 Ctx.channel.close 的区别](https://emacsist.github.io/2018/04/27/%E7%BF%BB%E8%AF%91netty4%E4%B8%AD-ctx.close-%E4%B8%8E-ctx.channel.close-%E7%9A%84%E5%8C%BA%E5%88%AB/)
```
让我们假设在 pipeline 里有三个 handlers , 它们都都拦截 close() 方法操作, 并且在面里调用 ctx.close()
ChannelPipeline p = ...;
p.addLast("A", new SomeHandler());
p.addLast("B", new SomeHandler());
p.addLast("C", new SomeHandler());
...
public class SomeHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
        ctx.close(promise);
    }
}
Channel.close() 会触发 C.close() , B.close(), A.clos(), 然后再关闭 channel
ChannelPipeline.context("C").close() 会触发 B.close(), A.close(), 然后再关闭 channel
ChannelPipeline.context("B").close() 会触发 A.close(), 然后再关闭 channel
ChannelPipeline.context("A").close() 则会直接关闭 channel. 不再会有 handlers 调用了.
因此, 什么时候应该使用 Channel.close() 和 ChannelHandlerContext.close() ? 最好规则如下:

如果你正写一个 ChannelHandler, 并且想在这个 handler 中关闭 channel, 则调用 ctx.close()
如果你正准备从一个外部的 handler (例如, 你有一个后台的非I/O线程, 并且你想从该线程中关闭连接). (译注: 这时是调用 Channel.close() ?)
总结
是不是, 整条流水线上, 如果后面的 handler 不用关心 close() 事件的话, 则用 ctx.close(), 否则用 channel.close().

就像官方文档中对 pipeline 的描述那样, ctx.write() 是从当前的 handler 中, 写到离它最近的 out handler 中, 而不是从流水线最后开始从头穿过处理一样~
```
- [netty ctx.write 和 ctx.channel.write 的区别](https://www.jianshu.com/p/46a53ba011a5)
```
结论：
ctx.channel().writeAndFlush 将从 Pipeline 的尾部开始往前找 OutboundHandler 。 
ctx.writeAndFlush 会从当前 handler 往前找 OutboundHandler。
```
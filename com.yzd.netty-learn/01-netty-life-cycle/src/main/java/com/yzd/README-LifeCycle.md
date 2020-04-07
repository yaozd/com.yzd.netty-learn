## Netty中Channel的生命周期
- [Netty中Channel的生命周期（SimpleChannelInboundHandler）](https://blog.csdn.net/u014131617/article/details/86476522) -首要参考byArvin
- [Netty入门与实战——channelHandler 的生命周期](https://blog.csdn.net/qq_32360995/article/details/91518229) -次要参考byArvin
- [netty中的ChannelHandler和ChannelPipeline](https://msd.misuland.com/pd/3181438578597036916)
```
1.channelRegistered
2.channelUnregistered
3.channelActive
4.channelInactive
5.channelReadComplete
6.userEventTriggered
7.channelWritabilityChanged
8.exceptionCaught
9.handlerAdded
10.handlerRemoved
11.channelRead0
12.执行顺序
```
## 执行顺序
- ChannelHandler 回调方法的执行顺序
    1.input
    ```
    handlerAdded() -> channelRegistered() -> channelActive() -> channelRead() -> channelReadComplete()
    PS
    a.handlerAdded()方法是指逻辑处理链在添加Handler时成功后回调的方法，表示当前处理链中添加了一个Handler。
    b.channelRegistered()方法指当一个连接建立一个连接后NIO会分配一个线程来管理这个连接，Hanlder已经绑定到该线程上。
    c.channelActive()方法表示所有Handler已经全部绑定到线程上，已经是一个完整的逻辑处理链时，会回调此方法。
    d.channelRead()方法表示当有数据可读时，也就是在我们客户端发来登陆请求数据时回调。
    e.channelReadComplete()方法表示一个完整的数据包读取完毕。
    ```
    2.output
    ```
    channelInactive() -> channelUnregistered() -> handlerRemoved()
    PS: 
    1. channelInactive(): 表面这条连接已经被关闭了。
    2. channelUnregistered()：连接关闭后，NIO也会收回线程对该连接的管理，那么这个Handler也就与该线程解除绑定。
    3. handlerRemoved()：最后将所有逻辑处理器都移除完毕后回调该方法。
    从handlerAdded()方法回调，到handlerRemoved()方法回调，就是一个完整的ChannelHandler的生命周期。
    ```

##　netty 处理远程主机强制关闭一个连接
- [netty 处理远程主机强制关闭一个连接](https://blog.csdn.net/weixin_34146410/article/details/92556344)
```
    //AllowHalfClosure:判断是否开启连接半关闭的功能
    //一个连接的远端关闭时本地端是否关闭
    //值为:false时(PS:默认值)，连接自动关闭。
    //值为:true时，触发 ChannelInboundHandler 的#userEventTriggered()方法，事件 ChannelInputShutdownEvent 。
    //ch.config().setAllowHalfClosure(true);
    //
    System.out.println("channel isAllowHalfClosure:"+ch.config().isAllowHalfClosure());
    -----------------------------------------------------------------------------------------------------
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("channel 用户事件触发-心跳检查：userEventTriggered()");
        if (evt instanceof ChannelInputShutdownEvent) {
            System.out.println("channel 用户事件触发-远程主机强制关闭连接:");
            //远程主机强制关闭连接
            //ctx.channel().close();
        }
        super.userEventTriggered(ctx, evt);
    }
```
## [ChannelPipeline和ChannelHandler总述](https://www.cnblogs.com/549294286/p/10790899.html)-netty 事件总
- 入站事件一般由I/O线程触发，以下事件为入站事件：
```
ChannelRegistered() // Channel注册到EventLoop
ChannelActive()     // Channel激活
ChannelRead(Object) // Channel读取到数据
ChannelReadComplete()   // Channel读取数据完毕
ExceptionCaught(Throwable)  // 捕获到异常
UserEventTriggered(Object)  // 用户自定义事件
ChannelWritabilityChanged() // Channnel可写性改变，由写高低水位控制
ChannelInactive()   // Channel不再激活
ChannelUnregistered()   // Channel从EventLoop中注销
```
- 出站事件一般由用户触发，以下事件为出站事件：
```
bind(SocketAddress, ChannelPromise) // 绑定到本地地址
connect(SocketAddress, SocketAddress, ChannelPromise)   // 连接一个远端机器
write(Object, ChannelPromise)   // 写数据，实际只加到Netty出站缓冲区
flush() // flush数据，实际执行底层写
read()  // 读数据，实际设置关心OP_READ事件，当数据到来时触发ChannelRead入站事件
disconnect(ChannelPromise)  // 断开连接，NIO Server和Client不支持，实际调用close
close(ChannelPromise)   // 关闭Channel
deregister(ChannelPromise)  // 从EventLoop注销Channel
```
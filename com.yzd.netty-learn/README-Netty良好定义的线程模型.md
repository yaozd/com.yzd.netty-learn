## 良好定义的线程模型
```
在3.x里并没有良好设计的线程模型，尽管曾经要修复线程模型在3.5的不一致性。4.0定义的一个严格的线程模型来帮助用户编写ChannelHandler而不必担心太多关于线程安全的东西。

Netty将不会再同步地调用ChannelHandler的方法了，除非ChannelHandler由@Shareable注解。这不会理会处理器方法的类似——入站、操作、或者是生命周期时间处理器方法。
用户不再需要同步入站或出站的事件处理器方法。
4.0不允许加入加入一个ChannelHandler超过一次，除非它由@Sharable注解。
每个由Netty调用的ChannelHandler的方法之间的关系总是happens-before。
用户不用定义一个volatile字段来保存处理器的状态。
用户能够在他加入一个处理器到ChannelPipeline的时候指定EventExecutor。
如果有指定，ChannelHandler的处理器方法总是由自动的EventExecutor来调用
如果没指定，处理器方法总是由它关联的Channel注册到的EventLoop来调用。
声明到一个处理器的EventExecutor和EventLoop总是单线程的。
处理器方法总是由相同线程调用。
如果指定了多线程的EventExecutor或EventLoop，线程中的一个会被选择，然后选择到的线程将会被使用，直到取消注册。
如果在相同管道里的两个处理器声明到不同的EventExecutor，它们会同时被调用。如果多个一个处理器去访问共享数据，用户需要注意线程安全，即使共享数据只能被相同管道里的处理器访问。
加入到ChannelFuture的ChannelFutureListener总是由关联到future相关的Channel的EventLoop线程调用。
不再有ExecutionHandler ——它包含到核心里
在你加入一个ChannelHandler到一个ChannelPipeline来告诉管道总是通过指定的EventExecutor调用加入的ChannelHander处理器的方法的时候，你可以指定一个EventExecutor。
//
1	Channel ch = ...;
2	ChannelPipeline p = ch.pipeline();
3	EventExecutor e1 = new DefaultEventExecutor(16);
4	EventExecutor e2 = new DefaultEventExecutor(8);
5	  
6	p.addLast(new MyProtocolCodec());
7	p.addLast(e1, new MyDatabaseAccessingHandler());
8	p.addLast(e2, new MyHardDiskAccessingHandler());
//
EventExecutor是EventLoop的超类，同时也继承了ScheduledExecutorService。
fixme
```
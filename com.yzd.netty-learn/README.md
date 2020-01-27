# [https://github.com/yaozd/com.yzd.netty-learn.git](https://github.com/yaozd/com.yzd.netty-learn.git)

# Netty-proxy
- netty4.x 实现接收http请求及响应
- netty实现简单的http代理
- NettyGateway-基于Netty编写的轻量级HTTP代理转发服务器，可以根据请求的内容关键字进行定制化的规则路由

## 推荐参考：
- [https://github.com/xuwujing/Netty-study](https://github.com/xuwujing/Netty-study)

## Netty的相关工程

- [netty-server-client](https://github.com/xuwujing/Netty/tree/master/Netty-hello) :Netty 的 HelloWord 工程。客户端和服务端通信的相关代码。

- [Netty-heartbeat](https://github.com/xuwujing/Netty/tree/master/Netty-heartbeat):Netty的心跳机制示例工程。

- [Netty-unpack](https://github.com/xuwujing/Netty/tree/master/Netty-unpack):Netty的粘包和拆包处理方法。

- [Netty-httpServer](https://github.com/xuwujing/Netty/tree/master/Netty-httpServer):Netty Http 服务的实现。

- [Netty-reconnect](https://github.com/xuwujing/Netty-study/tree/master/Netty-reconnect): Netty Client 重连机制的实现。

- [Netty-protobuf](https://github.com/xuwujing/Netty-study/tree/master/Netty-protobuf): Netty 使用protobuf 协议进行数据数据传输。

- [Netty-slidingWindow](https://github.com/xuwujing/Netty-study/tree/master/Netty-slidingWindow): Netty 结合滑动窗口使用示例。

- netty-schedule    :netty 的时间轮任务调度器
- netty-listener    :netty 监听

 
## Netty的相关博客

- [Netty 客户端与服务端通信](http://blog.csdn.net/qazwsxpcm/article/details/77750865)
- [Netty 客户端与服务端心跳](http://blog.csdn.net/qazwsxpcm/article/details/78174437)
- [Netty 粘包和拆包](http://blog.csdn.net/qazwsxpcm/article/details/78265120)
- [Netty HTTP服务](http://blog.csdn.net/qazwsxpcm/article/details/78364023)
- [Netty-SpringBott-Protobuf 服务](https://blog.csdn.net/qazwsxpcm/article/details/81069833)

## Netty总结
- [Netty浅析 - 1. 基础](https://www.jianshu.com/p/5e8e9d458c5c)
- [Netty浅析 - 2. 实现](https://www.jianshu.com/p/93c763786d0c) -包含Twitter对Netty池化缓存做的性能测试结果-首要参考byArvin
- [Netty浅析](https://www.jianshu.com/p/5f094e751f65)

## netty内存泄漏
- [netty内存泄漏，困扰了好几天的问题找到原文了](https://blog.csdn.net/hannuotayouxi/article/details/78827499) -推荐参考

## Netty实战思维导图
- [Netty实战思维导图](https://www.jianshu.com/p/d02cd754da1d)
- [Netty核心知识点总结（Netty实战读书笔记）](https://blog.csdn.net/wk52525/article/details/87931437)


## netty常用使用方式
- [netty常用使用方式](https://www.cnblogs.com/fairjm/p/netty_common_pattern.html) -推荐参考byArvin

## Netty源码工程
- [Netty的线程模型, 调优 及 献上写过注释的源码工程](https://www.cnblogs.com/ZhuChangwu/p/12011096.html)

## sharable 使用场景：
```
@sharable注解表明这个handler可以在多线程环境下使用， 当你自己写了一个非线程安全的handler， 不要去sharable， 否则自毁长城）
应该是一个关于连接无状态的，比如，对于拆包沾包handler，每个连接都应该有一个自己的对象，故不能在各连接复用一个这样的Sharable的handler
场景：     
1. 通常无状态的handler都是可以用这个标记的， 那么只需要存在一个handler instance， 减少资源开销      
2. 如你想对global的一些数据搞点事情， 那么就可以用这个了， 比如对ip的限制-UniqueIpFilter
3. 如果你需要全局统计一些信息，比如所有连接报错次数（exceptionCaught）等 PS:所有连接,则是无状态的
```
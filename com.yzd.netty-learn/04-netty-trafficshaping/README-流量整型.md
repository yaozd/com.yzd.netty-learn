## [Netty实现“流量整形”原理分析](https://www.jianshu.com/p/bea1b4ea8402)
```
流量整形
流量整形（Traffic Shaping）是一种主动调整流量输出速率的措施。
流量整形与流量监管的主要区别在于，流量整形对流量监管中需要丢弃的报文进行缓存——通常是将它们放入缓冲区或队列内，也称流量整形（Traffic Shaping，简称TS）。
当报文的发送速度过快时，首先在缓冲区进行缓存；再通过流量计量算法的控制下“均匀”地发送这些被缓冲的报文。
流量整形与流量监管的另一区别是，整形可能会增加延迟，而监管几乎不引入额外的延迟。

Netty提供了GlobalTrafficShapingHandler、ChannelTrafficShapingHandler、GlobalChannelTrafficShapingHandler三个类来实现流量整形，
他们都是AbstractTrafficShapingHandler抽象类的实现类，下面我们就对其进行介绍
ChannelTrafficShapingHandler : Channel 级别的
GlobalChannelTrafficShapingHandler : 全局级别的(无论打开多少Channel) 以及 每条 Channel 级别的
GlobalTrafficShapingHandler : 全局级别的
```
## 流量整形
- [Netty 那些事儿 ——— Netty实现“流量整形”原理分析及实战](https://www.jianshu.com/p/bea1b4ea8402) -github
    - [https://github.com/linling1/netty_module_function.git](https://github.com/linling1/netty_module_function.git)
- [Netty高级功能（一）：流控和流量整形](https://www.jianshu.com/p/6c4a7cbbe2b5)
- [Netty流量整形能解决内存泄漏问题吗](https://www.sohu.com/a/144188030_684743)
- [Netty 中的流控与统计](https://emacsist.github.io/2018/04/26/netty-%E4%B8%AD%E7%9A%84%E6%B5%81%E6%8E%A7%E4%B8%8E%E7%BB%9F%E8%AE%A1/) -含代码
- [Calculation of written and read bytes in netty application](https://stackoverflow.com/questions/31771800/calculation-of-written-and-read-bytes-in-netty-application)
- []()

## Netty 流量相关的包
```
io.netty.handler.traffic

ChannelTrafficShapingHandler : Channel 级别的
GlobalChannelTrafficShapingHandler : 全局级别的(无论打开多少Channel) 以及 每条 Channel 级别的
GlobalTrafficShapingHandler : 全局级别的
具体构造, 可以参考它们的相关构造函数和参数.它们都有与之相关的说明
```

## netty-高低水位线
- [Netty使用案例 -发送队列积压导致内存泄漏（一）](https://blog.csdn.net/u013642886/article/details/86632752) -首要参考-byArvin
- [Netty使用案例 -发送队列积压导致内存泄漏（二）](https://blog.csdn.net/u013642886/article/details/86633786)

### Netty-高水位线和低水位线是字节数，默认高水位是64K，低水位是32K
```
后端就会变得特别缓慢，如果这个时候前面的应用使用了一些无界的资源等，就有可能把自己弄死!!
https://www.jianshu.com/p/890525ff73cb
解：
其实ChannelOutboundBuffer虽然无界，但是可以给它配置一个高水位线和低水位线，当buffer的大小超过高水
位线的时候对应channel的isWritable就会变成false，当buffer的大小低于低水位线的时候，isWritable就会变成true。
所以应用应该判断isWritable，如果是false就不要再写数据了。高水位线和低水位线是字节数，默认高水位是64K，低水位是32K，
我们可以根据我们的应用需要支持多少连接数和系统资源进行合理规划。
.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 64 * 1024)
.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 32 * 1024)
```

### Netty发送队列积压案例-环境配置
```
//vm参数设置
-Xmx1000m  -XX:+PrintGC -XX:+PrintGCDetails
```

## 流量整形
- [Netty 那些事儿 ——— Netty实现“流量整形”原理分析及实战](https://www.jianshu.com/p/bea1b4ea8402)
- [Netty高级功能（一）：流控和流量整形](https://www.jianshu.com/p/6c4a7cbbe2b5)
- [Netty流量整形能解决内存泄漏问题吗](https://www.sohu.com/a/144188030_684743)
- [Netty 中的流控与统计](https://emacsist.github.io/2018/04/26/netty-%E4%B8%AD%E7%9A%84%E6%B5%81%E6%8E%A7%E4%B8%8E%E7%BB%9F%E8%AE%A1/)
- []()

## Netty 流量相关的包
```
io.netty.handler.traffic

ChannelTrafficShapingHandler : Channel 级别的
GlobalChannelTrafficShapingHandler : 全局级别的(无论打开多少Channel) 以及 每条 Channel 级别的
GlobalTrafficShapingHandler : 全局级别的
具体构造, 可以参考它们的相关构造函数和参数.它们都有与之相关的说明
```

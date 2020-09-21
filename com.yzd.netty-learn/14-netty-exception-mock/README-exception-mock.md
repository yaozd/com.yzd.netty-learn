# 异常模拟
## Netty 中 IOException: Connection reset by peer 与 java.nio.channels.ClosedChannelException: null
- [Netty 中 IOException: Connection reset by peer 与 java.nio.channels.ClosedChannelException: null](https://www.cnblogs.com/zemliu/p/3864131.html)
```
系统中出现了很多 IOException: Connection reset by peer 与 ClosedChannelException: null

深入看了看代码, 做了些测试, 发现 Connection reset 会在客户端不知道 channel 被关闭的情况下, 触发了 eventloop 的 unsafe.read() 操作抛出

而 ClosedChannelException 一般是由 Netty 主动抛出的, 在 AbstractChannel 以及 SSLHandler 里都可以看到 ClosedChannel 相关的代码
在代码的许多部分, 都会有这个 ClosedChannelException, 大概的意思是说在 channel close 以后, 如果还调用了 write 方法, 
则会将 write 的 future 设置为 failure, 并将 cause 设置为 ClosedChannelException, 同样 SSLHandler 中也类似
```
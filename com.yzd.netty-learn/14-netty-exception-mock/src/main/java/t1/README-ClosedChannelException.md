# ClosedChannelException
- [ java.nio.channels.ClosedChannelException: null](https://www.cnblogs.com/zemliu/p/3864131.html)
```
outbandBuffer !=null 会通过监听（ChannelFutureListener）的方式获取到ClosedChannelException:null异常！！
 ClosedChannelException 一般是由 Netty 主动抛出的, 在 AbstractChannel 以及 SSLHandler 里都可以看到 ClosedChannel 相关的代码
在代码的许多部分, 都会有这个 ClosedChannelException, 
大概的意思是说在 channel close 以后, 如果还调用了 write 方法, 
则会将 write 的 future 设置为 failure, 并将 cause 设置为 ClosedChannelException
```
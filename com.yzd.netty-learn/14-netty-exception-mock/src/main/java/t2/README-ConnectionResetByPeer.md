# IOException: Connection reset by peer
- [IOException: Connection reset by peer](https://www.cnblogs.com/zemliu/p/3864131.html)
```
发现 Connection reset 会在客户端不知道 channel 被关闭的情况下, 触发了 eventloop 的 unsafe.read() 操作抛出
//
Connection reset by peer, 要模拟这个情况比较简单, 
就是在 server 端设置一个在 channelActive 的时候就 close channel 的 handler. 
而在 client 端则写一个 Connect 成功后立即发送请求数据的 listener. 如下
```
## netty4handler的执行顺序一
- [netty4handler的执行顺序一](https://blog.csdn.net/fivestar2009/article/details/80493178)
```
 WangInHandler3-init
 WangInHandler3 channelRegistered
 WangInHandler3 channelActive
 WangInHandler3 channelRead
 WangInHandler3 channelReadComplete
 WangInHandler3 channelInactive
 WangInHandler3 channelUnregistered
 ----------------------------------
  WangInHandler3-init
  WangInHandler3 channelRegistered
  WangInHandler3 channelActive
  WangInHandler3 channelRead
  WangInHandler3 channelReadComplete
  WangInHandler3 channelInactive
  WangInHandler3 channelUnregistered

```
### Netty中Channel的生命周期（SimpleChannelInboundHandler）
- [Netty中Channel的生命周期（SimpleChannelInboundHandler）](https://blog.csdn.net/u014131617/article/details/86476522)

###　消息传递
```
ctx.fireChannelRead(msg)＝》InboundHandler之间传递，
ctx.writeAndFlush=》把InboundHandler消息传递到OutboundHandler
```

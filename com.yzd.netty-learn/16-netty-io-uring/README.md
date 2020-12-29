## 结论
- 目前此程没有正式运行成功

## io_uring（io_uring，由block IO大神Jens Axboe开发）
- [Netty/Incubator/Transport/Native/io_uring 0.0.1.Final released](https://netty.io/news/2020/11/16/io_uring-0-0-1-Final.html) -推荐参考byArvin(里面有DEMO)
- [Linux异步IO新时代：io_uring](https://yq.aliyun.com/articles/707076)

### 系统要求
```
need to run on x86_64 linux
need at least using Linux Kernel 5.9.0
```
### 实现
```
io_uring首先需要围绕高效进行设计。
为了避免在提交和完成事件中存在内存拷贝，
io_uring设计了一对共享的ring buffer用于应用和内核之间的通信。
其中，针对提交队列（SQ），应用是IO提交的生产者（producer），内核是消费者（consumer）；
反过来，针对完成队列（CQ），内核是完成事件的生产者，应用是消费者。
```

### 性能
- [io_uring比epoll提高了约3倍](https://netty.io/news/2020/11/16/io_uring-0-0-1-Final.html) 
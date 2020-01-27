- GC(Allocation Failure)引发的一些JVM知识点梳理
    - [GC(Allocation Failure)引发的一些JVM知识点梳理](https://blog.csdn.net/zc19921215/article/details/83029952)
    ```
    Allocation Failure：
    表明本次引起GC的原因是因为在年轻代中没有足够的空间能够存储新的数据了
    ```
- Full GC (Ergonomics)
    - [Full GC (Ergonomics) 产生的原因](https://blog.csdn.net/weixin_43194122/article/details/91526740)
    ```
    疑问说[Full GC (Ergonomics) 的Ergonomics究竟是个什么东东？
    Ergonomics翻译成中文，一般都是“人体工程学”。
    在JVM中的垃圾收集器中的Ergonomics就是负责自动的调解gc暂停时间和吞吐量之间的平衡，然后你的虚拟机性能更好的一种做法。
    对于注重吞吐量的收集器来说，在某个generation被过渡使用之前，GC ergonomics就会启动一次GC。
    ```
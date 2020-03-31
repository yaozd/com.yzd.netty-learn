## OpenTracing语义标准
- [OpenTracing语义标准](https://blog.csdn.net/qq924862077/article/details/80375426) -推荐参考byArvin
- [分布式全链路监控 -- opentracing小试](https://www.jianshu.com/p/82cd923191fb) -推荐参考byArvin
- [https://github.com/sofastack/sofa-tracer](https://github.com/sofastack/sofa-tracer)
```
每个Span包含以下的状态:（译者注：由于这些状态会反映在OpenTracing API中，所以会保留部分英文说明）

An operation name，操作名称
A start timestamp，起始时间
A finish timestamp，结束时间
Span Tag，一组键值对构成的Span标签集合。键值对中，键必须为string，值可以是字符串，布尔，或者数字类型。
Span Log，一组span的日志集合。 每次log操作包含一个键值对，以及一个时间戳。 键值对中，键必须为string，值可以是任意类型。 但是需要注意，不是所有的支持OpenTracing的Tracer,都需要支持所有的值类型。
SpanContext，Span上下文对象 (下面会详细说明)
References(Span间关系)，相关的零个或者多个Span（Span间通过SpanContext建立这种关系）
每一个SpanContext包含以下状态：

任何一个OpenTracing的实现，都需要将当前调用链的状态（例如：trace和span的id），依赖一个独特的Span去跨进程边界传输
Baggage Items，Trace的随行数据，是一个键值对集合，它存在于trace中，也需要跨进程边界传输
Span
表示分布式调用链条中的一个调用单元，比方说某个dubbo的调用provider，或者是个http调用的服务提供方，他的边界包含一个请求进到服务内部再由某种途径(http/dubbo等)从当前服务出去。一个span一般会记录这个调用单元内部的一些信息，例如：

日志信息
标签信息
开始/结束时间
SpanContext
表示一个span对应的上下文，span和spanContext基本上是一一对应的关系，上下文存储的是一些需要跨越边界的一些信息，例如：

spanId 当前这个span的id
traceId 这个span所属的traceId(也就是这次调用链的唯一id)
baggage 其他的能过跨越多个调用单元的信息
这个SpanContext可以通过某些媒介和方式传递给调用链的下游来做一些处理（例如子Span的id生成、信息的继承打印日志等等）
Tracer
tracer表示的是一个通用的接口，它相当于是opentracing标准的枢纽，它有以下的职责：

建立和开启一个span
从某种媒介中提取和注入一个spanContext
Carrier
表示的是一个承载spanContext的媒介，比方说在http调用场景中会有HttpCarrier，在dubbo调用场景中也会有对应的DubboCarrier。

Formatter
这个接口负责了具体场景中序列化反序列化上下文的具体逻辑，例如在HttpCarrier使用中通常就会有一个对应的HttpFormatter。Tracer的注入和提取就是委托给了Formatter

ScopeManager
这个类是0.30版本之后新加入的组件，这个组件的作用是能够通过它获取当前线程中启用的Span信息，并且可以启用一些处于未启用状态的span。在一些场景中，我们在一个线程中可能同时建立多个span，但是同一时间统一线程只会有一个span在启用，其他的span可能处在下列的状态中：

等待子span完成
等待某种阻塞方法
创建并未开始

```
## netty opentracing
- []()
- []()
## opentracing-API
- [opentracing/opentracing-java](https://github.com/opentracing/opentracing-java)
- [OpenTracing的使用实例（Java）](https://developer.aliyun.com/article/746760)
```
参考：
https://developer.aliyun.com/article/746760
//
构件组织
OpenTracing API的Java构件如下：

opentracing-api：主要的API，无其他依赖。
opentracing-noop：为主要API提供无意义实现（NoopTracer），依赖于opentracing-api。
opentracing-util：工具类，例如GlobalTracer和默认的基于ThreadLocal存储的ScopeManager实现，依赖于上面所有的构件。
opentracing-mock：用于测试的mock层。包含MockTracer，简单的将Span存储在内存中，依赖于opentracing-api和opentracing-noop。
```
## opentracing-netty
- [https://github.com/dougEfresh/opentracing-netty](https://github.com/dougEfresh/opentracing-netty)

##　OpenTracing的基本元素-三个概念
```
标记包含以下三个概念:
traceId
一次请求全局只有一个traceId。用来在海量的请求中找到同一链路的几次请求。比如servlet服务器接收到用户请求，调用dubbo服务，然后将结果返回给用户，整条链路只有一个traceId。开始于用户请求，结束于用户收到结果。
spanId
一个链路中每次请求都会有一个spanId。例如一次rpc，一次sql都会有一个单独的spanId从属于traceId。
parentId
上一次请求的spanId。用于将一条链路的多次请求串联起来。

```
## Zipkin示例-01
- 这条跟踪信息对应的代码片段为
```
Span twoPhase = tracer.newTrace().name("twoPhase").start();
try {
    Span prepare = tracer.newChild(twoPhase.context()).name("prepare").start();
    try {
	prepare();
    } finally {
	prepare.finish();
    }
    Span commit = tracer.newChild(twoPhase.context()).name("commit").start();
    try {
	commit();
    } finally {
	commit.finish();
    }
} finally {
    twoPhase.finish();
}
```
- 这段代码实际上向Zipkin上报的数据为
```
[
  {
    "traceId": "89e051d5394b90b1",
    "id": "89e051d5394b90b1",
    "name": "twophase",
    "timestamp": 1510043591038983,
    "duration": 1000356,
    "binaryAnnotations": [
      {
        "key": "lc",
        "value": "",
        "endpoint": {
          "serviceName": "tracer-demo",
          "ipv4": "192.168.99.1"
        }
      }
    ]
  },
  {
    "traceId": "89e051d5394b90b1",
    "id": "60568c4903793b8d",
    "name": "prepare",
    "parentId": "89e051d5394b90b1",
    "timestamp": 1510043591039919,
    "duration": 499246,
    "binaryAnnotations": [
      {
        "key": "lc",
        "value": "",
        "endpoint": {
          "serviceName": "tracer-demo",
          "ipv4": "192.168.99.1"
        }
      }
    ]
  },
  {
    "traceId": "89e051d5394b90b1",
    "id": "ce14448169d01d2f",
    "name": "commit",
    "parentId": "89e051d5394b90b1",
    "timestamp": 1510043591539304,
    "duration": 499943,
    "binaryAnnotations": [
      {
        "key": "lc",
        "value": "",
        "endpoint": {
          "serviceName": "tracer-demo",
          "ipv4": "192.168.99.1"
        }
      }
    ]
  }
]
```　
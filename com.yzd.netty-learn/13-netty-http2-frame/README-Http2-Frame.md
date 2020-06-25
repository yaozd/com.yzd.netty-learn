## https://netty.io/

### 参考：
- [使用Netty创建HTTP/2服务器](https://www.kernelhcy.info/?p=142)
```
1. TLS
虽然HTTP/2协议不强制使用TLS，理论上可以不使用TLS。但是目前主流的浏览器都是默认使用TLS的，所以最好使用TLS。


在使用TLS的时候，需要TLS支持ALPN协议，ALPN(Application-Layer Protocol Negotiation，应用层协议协商，RFC7301)是TLS的一个扩展，
用于客户端和服务器端之间协商所使用的HTTP协议版本。ALPN比较新，JDK中的TLS不支持ALPN，OpenSSL从1.0.2版本开始支持。
为了支持ALPN，需要在服务器上安装1.0.2版本及以上的OpenSSL，或者使用Jetty-ALPN。本文中使用OpenSSL。

当前版本的Netty在使用HTTP/2时，必须使用ALPN协议。

2. NETTY-TCNATIVE
netty-tcnative库fork自Apache Tomcat Native Library，
是一个针对native资源的java层封装库。其中包含有针对OpenSSL的封装。至少需要1.1.33.Fork7版本。

添加依赖
首先添加netty依赖：

<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.20.Final</version>
</dependency>

<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.20.Final</version>
</dependency>
使用最新版本即可。

然后添加netty-tcnative依赖。netty-tcnative需要根据不同的系统引入不同的版本。使用os-maven-plugin来检测系统类型和版本。具体配置如下：

<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative</artifactId>
    <version>2.0.7.Final</version>
    <classifier>${os.detected.classifier}</classifier>
</dependency>

<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.5.0.Final</version>
        </extension>
    </extensions>
</build>
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative</artifactId>
    <version>2.0.7.Final</version>
    <classifier>${os.detected.classifier}</classifier>
</dependency>
 
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.5.0.Final</version>
        </extension>
    </extensions>
</build>
os-maven-plugin会根据当前系统的版本和类型，
创建变量os.detected.classifier。netty-tcnative的详细使用可参考http://netty.io/wiki/forked-tomcat-native.html。
```
- [https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example/http2/helloworld/frame](https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example/http2/helloworld/frame)

## 测试服务

> 通过header中的uuid对请进行跟踪

## 使用场景

> 自动化测试中功能验证


## 参考
- [maven-shade-plugin使用记录 (设置main class, 排除META-INF/*.SF等文件)](https://blog.csdn.net/zifanyou/article/details/85069819)
- [Java 设置系统参数和运行参数](https://blog.csdn.net/moonspiritacm/article/details/80189301)
- [SpringBoot中log4j2、logback读取JVM启动参数 并 指定默认值](https://blog.csdn.net/m0_38135981/article/details/85341138)
```
jvm 启动参数如下
-Dapp.log.level=info
<property name="LOG_LEVEL" value="${app.log.level:-info}"/>
<root level="${LOG_LEVEL}">
        <appender-ref ref="stdout"/>
</root>
注意
" :- " 操作符为指定默认值。
例如 " :-. " 默认目录为"." (当前目录)
```
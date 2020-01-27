## Attribute
- [Netty学习笔记15 Netty Attribute使用](https://blog.csdn.net/xundh/article/details/79360563)
- [Netty 4.0中的新变化和注意点](http://www.360doc.com/content/15/1204/14/15099545_517869298.shtml)
- []()
- 在Channel上绑定链接上下文信息，类似Session的功能，存储在Channel中的Attr中。
    ```
    public class ChannelUtils {
        public static ClientContext getOrCreate(Channel socketChannel) {
            AttributeKey<ClientContext> key = AttributeKey.valueOf(ClientContext.class.toString());
            Attribute<ClientContext> attr = socketChannel.attr(key);
            if (attr.get() == null) {
                attr.set(createClientContext(socketChannel));
            }
            return attr.get();
        }
    
        private static ClientContext createClientContext(Channel socketChannel) {
            return new DefaultClientContext(socketChannel.pipeline());
        }
    }
    ```
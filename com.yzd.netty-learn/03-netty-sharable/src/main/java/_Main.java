import io.netty.channel.ChannelHandler;
import org.reflections.Reflections;

import java.util.Set;

/**
 * @Author: yaozh
 * @Description:
 */
public class _Main {
    //以下是所有 Netty 包下, Sharable 的handler:
    //Netty资料收集与整理
    //https://emacsist.github.io/2018/04/24/netty%E8%B5%84%E6%96%99%E6%94%B6%E9%9B%86%E4%B8%8E%E6%95%B4%E7%90%86/
    public static void main(String[] args) {
        //findSharable
        Set<Class<?>> set = new Reflections("io.netty.handler").getTypesAnnotatedWith(ChannelHandler.Sharable.class);
        System.out.println("sharable class size =>  " + set.size());
        for (Class<?> clazz : set) {
            System.out.println("sharable class => " + clazz.getName());
        }
    }
}

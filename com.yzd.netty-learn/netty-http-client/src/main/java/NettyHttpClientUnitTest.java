import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.example.demo.httpclient.NettyHttpClient;
import com.example.demo.httpclient.RequestUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author: yaozhendong
 * @create: 2019-11-29 11:07
 **/
@Slf4j
public class NettyHttpClientUnitTest {
    private static String requestUrl = "http://127.0.0.1:8085/";
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();
    static Channel channel;
    static URI uri;

    @BeforeClass
    public static void runBeforeTestMethod() throws URISyntaxException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("root");
        logger.setLevel(Level.toLevel("INFO"));
        //
        uri = new URI(requestUrl);
        channel = NettyHttpClient.getInstance().getChannel(uri);
        log.info("========数据包持续发送");
    }

    /**
     * 持续发送数据包
     */
    @Test
    @PerfTest(threads = 1, invocations = 1000000)
    public void continueSendPackageWithNettyHttpClient() {
        //channel关闭就直接退出
        if (!channel.isActive()) {
            //Assert.assertTrue(false);
        }
        //Assert.assertTrue(false);
        try {
            channel.writeAndFlush(RequestUtil.getRequestPackageForPostMethod(uri, "HELLO WORLD"));
            /**
             //channel.closeFuture();
             //channel.close();
             */
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}

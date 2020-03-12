package com.yzd.resolve;

import com.yzd.client.NettyHttpClient;
import com.yzd.client.RequestUtil;
import com.yzd.resolve.data.RequestData;
import com.yzd.resolve.data.TaskInfo;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author yaozh
 */
public class ResolverScheduler {
    private static EventLoopGroup work = new NioEventLoopGroup();

    /**
     *
     * @param requestData
     */
    public static void doWork(RequestData requestData) {
        work.schedule(() -> {
            StringBuilder stringBuilder = new StringBuilder()
                    .append(requestData.getRequestType())
                    .append(":")
                    .append(requestData.getTaskInfo().getUuid())
                    .append(":")
                    .append(RequestUtil.getUri(requestData));
            System.out.println(stringBuilder.toString());
            NettyHttpClient.getInstance().writeData(requestData);
        }, requestData.getIntervalTime(), TimeUnit.MILLISECONDS);
    }
}

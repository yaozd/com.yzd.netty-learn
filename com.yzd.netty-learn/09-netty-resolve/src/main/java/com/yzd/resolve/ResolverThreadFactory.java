package com.yzd.resolve;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
public class ResolverThreadFactory implements ThreadFactory {

    public static final String PREFIX = "resolver-thread-";

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, PREFIX + THREAD_COUNT.incrementAndGet());
    }
}

package com.yzd.netty;

import io.netty.util.ReferenceCounted;

/**
 * 释放数据
 */
public class App {
    /**
     * 释放数据
     * @param data
     */
    public void releaseData(Object data) {
        if (data instanceof ReferenceCounted) {
            ReferenceCounted referenceCounted = (ReferenceCounted)data;
            if (referenceCounted.refCnt() > 0) {
                referenceCounted.release();
            }
        }
    }
}

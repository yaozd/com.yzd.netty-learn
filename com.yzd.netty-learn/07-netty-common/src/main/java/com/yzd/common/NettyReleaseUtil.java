package com.yzd.common;

import io.netty.util.ReferenceCounted;

/**
 * @Author: yaozh
 * @Description:
 */
public class NettyReleaseUtil {
    /**
     * 释放数据，防止Netty的ByteBuff内存泄漏问题
     * netty LEAK: ByteBuf.release() was not called before it's garbage-collected
     * https://www.cnblogs.com/yoyotl/p/8433135.html
     * PS:增加内存检查
     * 泄露检测级别
     * 当前有4个泄露检测级别：
     * 禁用（DISABLED）   - 完全禁止泄露检测。不推荐。
     * 简单（SIMPLE）       - 告诉我们取样的1%的缓冲是否发生了泄露。默认。
     * 高级（ADVANCED） - 告诉我们取样的1%的缓冲发生泄露的地方
     * 偏执（PARANOID）  - 跟高级选项类似，但此选项检测所有缓冲，而不仅仅是取样的那1%。此选项在自动测试阶段很有用。如果构建（build）输出包含了LEAK，可认为构建失败。
     * 原文链接：https://blog.csdn.net/hannuotayouxi/article/details/78827499
     * ————————————————
     * API-ROUTER测试环境配置
     * -Dio.netty.leakDetection.level=PARANOID -Dio.netty.leakDetection.targetRecords=15
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
    /**
     * 完全释放数据
     * referenceCounted refCnt=0
     * 暂时是针对htt2数据流考虑
     * @param data
     */
    public static void completelyReleaseData(Object data) {
        if (data instanceof ReferenceCounted) {
            ReferenceCounted referenceCounted = (ReferenceCounted) data;
            if (referenceCounted.refCnt() > 0) {
                while (!referenceCounted.release()) {
                }
            }
        }
    }
}

package com.iflytek.edu.alloc;

import io.netty.buffer.ByteBuf;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/4
 */
public interface RecvByteBufAllocator {
    ByteBuf allocate();
    boolean continueReading();
    void recordBytesRead(int attemptBytesRead, int realBytesRead);
    void recordMessageRead(int messageNum);
}

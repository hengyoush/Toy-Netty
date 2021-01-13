package io.yhheng.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/4
 */
public interface RecvByteBufAllocator {
    ByteBuf allocate(ByteBufAllocator byteBufAllocator);
    boolean continueReading();
    void recordBytesRead(int attemptBytesRead, int realBytesRead);
    void recordMessageRead(int messageNum);
}

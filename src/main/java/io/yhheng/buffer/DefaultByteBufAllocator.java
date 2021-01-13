package io.yhheng.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public abstract class DefaultByteBufAllocator implements RecvByteBufAllocator {
    private int maxMessagesPerRead;
    private int readMessageNum;
    private int totalReadBytes;
    private int lastReadBytes;
    private int attemptReadBytes;

    public DefaultByteBufAllocator(int maxMessagesPerRead) {
        this.maxMessagesPerRead = maxMessagesPerRead;
    }

    @Override
    public ByteBuf allocate(ByteBufAllocator byteBufAllocator) {
        return byteBufAllocator.ioBuffer(guess());
    }

    @Override
    public boolean continueReading() {
        return (attemptReadBytes == lastReadBytes) && readMessageNum < maxMessagesPerRead && totalReadBytes > 0;
    }

    @Override
    public void recordBytesRead(int attemptBytesRead, int realBytesRead) {
        this.attemptReadBytes = attemptBytesRead;
        this.lastReadBytes = realBytesRead;
        totalReadBytes += attemptReadBytes;
    }

    @Override
    public void recordMessageRead(int messageNum) {
        readMessageNum += messageNum;
    }

    abstract int guess();
}

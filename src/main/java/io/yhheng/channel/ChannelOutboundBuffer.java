package io.yhheng.channel;

import io.yhheng.concurrent.Promise;

import java.nio.ByteBuffer;

public interface ChannelOutboundBuffer {
    void addMessage(Object msg, int size, Promise<Void> promise);
    void addFlush();
    Object current();
    boolean remove();
    boolean remove(Throwable cause);
    void close(Throwable cause);
    void removeBytes(long removeBytes);

    int size();
    boolean isEmpty();

    /**
     * 从ChannelOutboundBuffer拿出最大maxCount个buffer，并且最大字节数不超过maxBytes
     * @param maxCount 返回值最大大小
     * @param maxBytes 最大字节数，返回值可能会超过，因为我们确保返回至少一个buffer
     * @return
     */
    ByteBuffer[] nioBuffers(int maxCount, long maxBytes);
    int nioBufferCount();
}

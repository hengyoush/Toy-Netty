package io.yhheng.channel;

import io.yhheng.concurrent.Promise;

public interface ChannelOutboundBuffer {
    void addMessage(Object msg, int size, Promise<Void> promise);
    void addFlush();
    Object current();
    boolean remove();
    boolean remove(Throwable cause);
}

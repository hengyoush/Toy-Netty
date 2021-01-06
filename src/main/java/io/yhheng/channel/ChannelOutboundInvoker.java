package io.yhheng.channel;

import io.yhheng.concurrent.Promise;

import java.net.SocketAddress;

public interface ChannelOutboundInvoker {
    void bind(SocketAddress socketAddress, Promise<Void> promise);
    void connect(SocketAddress socketAddress, Promise<Void> promise);
    void write(Object msg, Promise<Void> promise);
    void flush(Promise<Void> promise);
    void close(Promise<Void> promise);
    void unregister(Promise<Void> promise);
}

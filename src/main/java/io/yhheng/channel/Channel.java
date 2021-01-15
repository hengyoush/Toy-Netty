package io.yhheng.channel;


import io.yhheng.concurrent.Promise;
import io.yhheng.eventloop.EventLoop;

import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

/**
 * @version V1.0
 * @author yhheng
 * @date 2020/12/30
 */
public interface Channel extends ChannelOutboundInvoker {
    /**
     * must invoke from IO thread
     * @param socketAddress
     * @param promise
     */
    void connect(SocketAddress socketAddress, Promise<Void> promise);
    void finishConnect();
    void bind(SocketAddress socketAddress, Promise<Void> promise);

    void unregister(Promise<Void> promise);
    void register(EventLoop eventLoop, Promise<Void> promise);

    void read();
    void beginRead();
    void write(Object msg, Promise<Void> promise);
    void flush(Promise<Void> promise);

    void close(Promise<Void> promise);

    EventLoop eventloop();
    ChannelPipeline pipeline();
    SelectableChannel javaChannel();
    ChannelConfig config();
    Promise<Void> voidPromise();

    boolean isRegistered();
    boolean isOpen();
    boolean isActive();
}

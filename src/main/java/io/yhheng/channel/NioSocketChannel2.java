package io.yhheng.channel;


import io.yhheng.concurrent.DefaultPromise;
import io.yhheng.concurrent.Promise;
import io.yhheng.eventloop.EventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 与ServerSocket有如下不同：
 * 1.close的时候要注意connect同时进行的情况
 * 2.write connect bind isActive的不同
 *
 * @version V1.0
 * @author yhheng
 * @date 2020/12/30
 */
public class NioSocketChannel2 implements Channel {
    private ServerSocketChannel serverSocketChannel;
    private EventLoop eventLoop;
    private ChannelPipeline pipeline;

    private Promise<Void> closePromise = new DefaultPromise<>();
    private Promise<Void> voidPromise = new DefaultPromise<>();
    private boolean closeInited = false;
    private boolean registered = false;
    private boolean firstRegistered = true;

    public NioSocketChannel2(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void close(Promise<Void> promise) {
        if (closeInited) {
            if (closePromise.isDone()) {
                return;
            } else {
                closePromise.addListener(future -> promise.trySuccess(null));
            }
            // 已经关闭，直接返回
            return;
        }

        // 开始关闭
        closeInited = true;

        // doClose
        try {
            serverSocketChannel.close();
            closePromise.setSuccess(null);
            promise.trySuccess(null);
        } catch (IOException e) {
            closePromise.setFail(e);
            promise.tryFail(e);
        }
    }

    @Override
    public void unregister(Promise<Void> promise) {

    }

    @Override
    public void write(Promise<Void> promise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(SocketAddress socketAddress, Promise<Void> promise) {
        boolean wasActive = isActive();
        try {
            serverSocketChannel.bind(socketAddress);

            if (!wasActive && isActive()) {
                // fire channel Active
                pipeline().fireChannelActive();
            }
        } catch (IOException e) {
            promise.tryFail(e);
            if (!isOpen()) {
                close(voidPromise);
            }
        }
    }

    @Override
    public void connect(SocketAddress socketAddress, Promise<Void> promise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush(Promise<Void> promise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void read() {
        // fire channel read, message is channel
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(EventLoop eventLoop, Promise<Void> promise) {
        if (isRegistered()) {
            promise.setFail(new IllegalStateException("already registered"));
            return;
        }

        try {
            if (eventLoop.inEventLoop()) {
                register0(eventLoop, promise);
            } else {
                eventLoop.submit(() -> {
                    register0(eventLoop, promise);
                });
            }
        } catch (Throwable e) {
            // close channel
            close(voidPromise);
        }
    }

    private void register0(EventLoop eventLoop, Promise<Void> promise) {
        if (!isOpen()) {
            promise.setFail(new IllegalStateException("channel is not open"));
        }

        boolean firstRegistered = this.firstRegistered;
        try {
            this.eventLoop = eventLoop;
            serverSocketChannel.register(eventLoop.selector(), 0, this);
            this.firstRegistered = false;
            this.registered = true;
            promise.trySuccess(null);
            pipeline().fireChannelRegistered();

            if (isActive()) {
                if (firstRegistered) {
                    pipeline.fireChannelActive();
                } else {
                    // TODO 注册Accept事件
                }
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EventLoop eventloop() {
        return eventLoop;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public boolean isOpen() {
        return serverSocketChannel.isOpen();
    }

    @Override
    public boolean isActive() {
        return isOpen() && serverSocketChannel.socket().isBound();
    }
}

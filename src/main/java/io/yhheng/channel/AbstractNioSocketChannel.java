package io.yhheng.channel;

import io.netty.util.internal.ThrowableUtil;
import io.yhheng.common.ConnectTimeoutException;
import io.yhheng.common.ConnectionPendingException;
import io.yhheng.common.ExtendedClosedChannelException;
import io.yhheng.concurrent.DefaultPromise;
import io.yhheng.concurrent.Future;
import io.yhheng.concurrent.Promise;
import io.yhheng.eventloop.EventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNioSocketChannel implements Channel {
    private SocketChannel ch;
    private EventLoop eventLoop;
    private ChannelPipeline pipeline;
    private ChannelConfig config;
    private ChannelOutboundBuffer channelOutboundBuffer;

    private SelectionKey selectionKey;

    private Promise<Void> connectPromise = new DefaultPromise<>();
    private Future<?> connectTimeoutFuture;
    private Promise<Void> closePromise = new DefaultPromise<>();
    private Promise<Void> voidPromise = new DefaultPromise<>();
    private boolean closeInited = false;
    private boolean registered = false;
    private boolean firstRegistered = true;

    @Override
    public void connect(SocketAddress remoteAddress, Promise<Void> promise) {
        if (!isOpen()) {
            promise.tryFail(ThrowableUtil.unknownStackTrace(
                    new ExtendedClosedChannelException(), AbstractNioSocketChannel.class, "ensureOpen(...)"));
            return;
        }

        try {
            if (connectPromise != null) {
                throw new ConnectionPendingException();
            }

            boolean wasActive = isActive();
            if (doConnect(remoteAddress)) {
                fulfillConnectPromise(promise, wasActive);
            } else {
                // 非阻塞模式
                connectPromise = promise;
                connectTimeoutFuture = eventLoop.schedule(() -> {
                    // 超时仍未连接上那么就立即关闭
                    Promise<Void> connectPromise = AbstractNioSocketChannel.this.connectPromise;
                    if (connectPromise != null && connectPromise.tryFail(new ConnectTimeoutException())) {
                        close(voidPromise);
                    }
                }, config.connectTimeoutMills(), TimeUnit.MILLISECONDS);
            }
        } catch (Throwable e) {
            promise.tryFail(new ExtendedClosedChannelException(e));
            if (!isOpen()) {
                close(voidPromise);
            }
        }
    }

    abstract boolean doConnect(SocketAddress remoteAddress);

    private void fulfillConnectPromise(Promise<Void> promise, boolean wasActive) {
        boolean promiseSet = promise.trySuccess(null);

        if (wasActive && isActive()) {
            pipeline.fireChannelActive();
        }

        if (!promiseSet) {
            close(voidPromise);
        }
    }

    @Override
    public void finishConnect() {
        boolean wasActive = isActive();
        try {
            doFinishConnect();
            fulfillConnectPromise(connectPromise, wasActive);
        } catch (Exception e) {
            connectPromise.tryFail(new ExtendedClosedChannelException(e));
            if (!isOpen()) {
                close(voidPromise);
            }
        } finally {
            if (connectTimeoutFuture != null) {
                connectTimeoutFuture.cancel(true);
            }
            connectPromise = null;
        }
    }

    abstract void doFinishConnect();
    @Override
    public void bind(SocketAddress socketAddress, Promise<Void> promise) {
        if (!isOpen()) {
            promise.tryFail(ThrowableUtil.unknownStackTrace(
                    new ExtendedClosedChannelException(), AbstractNioSocketChannel.class, "ensureOpen(...)"));
            return;
        }

        try {
            boolean wasActive = isActive();
            doBind(socketAddress);
            if (!wasActive && isActive()) {
                invokeLater(() -> pipeline.fireChannelActive());
            }
            promise.trySuccess(null);
        } catch (Throwable t) {
            promise.tryFail(new ExtendedClosedChannelException());
            if (!isOpen()) {
                close(voidPromise);
            }
        }
    }

    abstract boolean doBind(SocketAddress localAddress);

    private void invokeLater(Runnable command) {
        try {
            eventloop().submit(command);
        } catch (Throwable e) {

        }
    }

    @Override
    public void unregister(Promise<Void> promise) {
        if (!registered) {
            promise.trySuccess(null);
        }

        boolean wasActive = isActive();
        invokeLater(() -> {
            selectionKey.cancel();
            if (wasActive && !isActive()) {
                pipeline.fireChannelInActive();
            }
            promise.trySuccess(null);
        });
    }

    @Override
    public void register(EventLoop eventLoop, Promise<Void> promise) {
        if (isRegistered()) {
            promise.setFail(new IllegalStateException("already registered"));
            return;
        }

        try {
            if (!isOpen()) {
                promise.setFail(new IllegalStateException("channel is not open"));
            }
            boolean firstRegistered = this.firstRegistered;

            register0();

            this.firstRegistered = false;
            this.registered = true;
            promise.trySuccess(null);
            pipeline().fireChannelRegistered();

            if (isActive()) {
                if (firstRegistered) {
                    pipeline.fireChannelActive();
                } else {
                    beginRead();
                }
            }
        } catch (Throwable t) {
            close(voidPromise);
        }
    }

    abstract void register0();

    abstract void beginRead();

    @Override
    public abstract void read();

    @Override
    public void write(Object msg, Promise<Void> promise) {
        ChannelOutboundBuffer channelOutboundBuffer = this.channelOutboundBuffer;
        int size = size(msg);
        channelOutboundBuffer.addMessage(msg, size, promise);
    }

    private int size(Object msg) {
        // TODO
        return 0;
    }

    @Override
    public void flush(Promise<Void> promise) {
        this.channelOutboundBuffer.addFlush();
        flush0();
    }

    private void flush0() {
        try {
            doWrite(this.channelOutboundBuffer);
        } catch (Exception e) {
            if (e instanceof IOException && config.isAutoClose()) {
                close(voidPromise);
            } else {
                this.channelOutboundBuffer = null;
                try {
                    ch.shutdownOutput();
                } catch (IOException ex) {
                    close(voidPromise);
                }
            }
        }
    }

    abstract void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception;

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

        try {
            ch.close();
            closePromise.setSuccess(null);
            promise.trySuccess(null);
        } catch (IOException e) {
            closePromise.setFail(e);
            promise.tryFail(e);
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
    public abstract boolean isOpen();

    @Override
    public abstract boolean isActive();
}
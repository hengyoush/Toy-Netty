package io.yhheng.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.net.SocketAddress;

public class DefaultChannelPipeline implements ChannelPipeline {
    private Channel channel;

    private final AbstractChannelHandlerContext head;
    private final AbstractChannelHandlerContext tail;

    public DefaultChannelPipeline(Channel channel) {
        this.channel = channel;
        this.head = new HeadContext(this);
        this.tail = new TailContext(this);
    }

    @Override
    public ChannelPipeline addLast(ChannelHandler handler) {
        DefaultChannelHandlerContext context = new DefaultChannelHandlerContext(this, handler);
        AbstractChannelHandlerContext prev = tail.prev;
        prev.next = context;
        context.next = tail;
        tail.prev = context;
        context.prev = prev;
        return this;
    }

    @Override
    public ChannelPipeline fireChannelRegistered() {
        head.fireChannelRegistered();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelUnRegister() {
        head.fireChannelUnRegister();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelActive() {
        head.fireChannelActive();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelInActive() {
        head.fireChannelInActive();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelRead(Object msg) {
        head.fireChannelRead(msg);
        return this;
    }

    @Override
    public ChannelPipeline fireExceptionCaught(Throwable cause) {
        head.fireExceptionCaught(cause);
        return this;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise promise) {
        tail.bind(socketAddress, promise);
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise promise) {
        tail.connect(socketAddress, promise);
        return promise;
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        tail.write(msg, promise);
        return promise;
    }

    @Override
    public ChannelFuture flush(ChannelPromise promise) {
        tail.flush(promise);
        return promise;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        tail.close(promise);
        return promise;
    }

    @Override
    public ChannelFuture unregister(ChannelPromise promise) {
        tail.unregister(promise);
        return promise;
    }

    /**
     * HeadContext 作为InBoundHandler，将事件向后传递
     * 作为OutBoundHandler，进行具体的IO操作
     */
    private static class HeadContext extends AbstractChannelHandlerContext implements
            ChannelInboundHandler, ChannelOutboundHandler {

        public HeadContext(ChannelPipeline channelPipeline) {
            super(channelPipeline);
            init();
        }

        @Override
        public ChannelHandler handler() {
            return this;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext context) {
            context.fireChannelRegistered();
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext context) {
            context.fireChannelUnRegister();
        }

        @Override
        public void channelActive(ChannelHandlerContext context) {
            context.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            context.fireChannelInActive();
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object msg) {
            context.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            context.fireExceptionCaught(cause);
        }

        @Override
        public void bind(ChannelHandlerContext context, SocketAddress socketAddress, ChannelPromise promise) {
            context.channel().bind(socketAddress, promise);
        }

        @Override
        public void unregister(ChannelHandlerContext context, ChannelPromise promise) {
            context.channel().unregister(promise);
        }

        @Override
        public void connect(ChannelHandlerContext context, SocketAddress socketAddress, ChannelPromise promise) {
            context.channel().connect(socketAddress, promise);
        }

        @Override
        public void write(ChannelHandlerContext context, Object msg, ChannelPromise promise) {
            context.channel().write(msg, promise);
        }

        @Override
        public void read(ChannelHandlerContext context, ChannelPromise promise) {
            context.channel().beginRead();
        }

        @Override
        public void flush(ChannelHandlerContext context, ChannelPromise promise) {
            context.channel().flush(promise);
        }

        @Override
        public void close(ChannelHandlerContext context, ChannelPromise promise) {
            context.channel().close(promise);
        }
    }

    /**
     * TailContext作为ChannelInboundHandler处理到达最底端的入站事件，对其进行兜底处理
     */
    private static class TailContext extends AbstractChannelHandlerContext implements ChannelInboundHandler {

        public TailContext(ChannelPipeline channelPipeline) {
            super(channelPipeline);
            init();
        }

        @Override
        public ChannelHandler handler() {
            return this;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext context) {

        }

        @Override
        public void channelUnregistered(ChannelHandlerContext context) {

        }

        @Override
        public void channelActive(ChannelHandlerContext context) {

        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {

        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object msg) {
            ReferenceCountUtil.release(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {

        }
    }
}

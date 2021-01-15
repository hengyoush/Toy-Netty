package io.yhheng.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

public class DefaultChannelPipeline implements ChannelPipeline {


    @Override
    public ChannelPipeline addLast(ChannelHandler handler) {
        return null;
    }

    @Override
    public ChannelPipeline fireChannelRegistered() {
        return null;
    }

    @Override
    public ChannelPipeline fireChannelUnRegister() {
        return null;
    }

    @Override
    public ChannelPipeline fireChannelActive() {
        return null;
    }

    @Override
    public ChannelPipeline fireChannelInActive() {
        return null;
    }

    @Override
    public ChannelPipeline fireChannelRead(Object msg) {
        return null;
    }

    @Override
    public ChannelPipeline fireExceptionCaught(Throwable cause) {
        return null;
    }

    @Override
    public Channel channel() {
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture flush(ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return null;
    }

    @Override
    public ChannelFuture unregister(ChannelPromise promise) {
        return null;
    }

    /**
     * HeadContext 作为InBoundHandler，将事件向后传递
     * 作为OutBoundHandler，进行具体的IO操作
     */
    private static class HeadContext extends AbstractChannelHandlerContext implements
            ChannelInboundHandler, ChannelOutboundHandler {

        public HeadContext(ChannelPipeline channelPipeline) {
            super(channelPipeline);
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
}

package io.yhheng.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {
    private static final int OUTBOUND_MASK = 1 << 1;
    private static final int INBOUND_MASK = 1 << 1;

    private final Channel channel;
    private final ChannelPipeline channelPipeline;

    AbstractChannelHandlerContext next;
    AbstractChannelHandlerContext prev;

    int mask;

    public AbstractChannelHandlerContext(ChannelPipeline channelPipeline) {
        this.channel = channelPipeline.channel();
        this.channelPipeline = channelPipeline;
        if (handler() != null) {
            ChannelHandler handler = handler();
            if (handler instanceof ChannelInboundHandler) {
                this.mask |= INBOUND_MASK;
            }

            if (handler instanceof ChannelOutboundHandler) {
                this.mask |= OUTBOUND_MASK;
            }
        }
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public abstract ChannelHandler handler();

    @Override
    public ChannelPipeline pipeline() {
        return channelPipeline;
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        AbstractChannelHandlerContext context = findContextInbound();
        ((ChannelInboundHandler) context.handler()).channelRegistered(context);
        return this;
    }

    @Override
    public ChannelInboundInvoker fireChannelUnRegister() {
        AbstractChannelHandlerContext context = findContextInbound();
        ((ChannelInboundHandler) context.handler()).channelUnregistered(context);
        return this;
    }

    @Override
    public ChannelInboundInvoker fireChannelActive() {
        AbstractChannelHandlerContext context = findContextInbound();
        ((ChannelInboundHandler) context.handler()).channelActive(context);
        return this;
    }

    @Override
    public ChannelInboundInvoker fireChannelInActive() {
        AbstractChannelHandlerContext context = findContextInbound();
        ((ChannelInboundHandler) context.handler()).channelInactive(context);
        return this;
    }

    @Override
    public ChannelInboundInvoker fireChannelRead(Object msg) {
        AbstractChannelHandlerContext context = findContextInbound();
        ((ChannelInboundHandler) context.handler()).channelRead(context, msg);
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise promise) {
        AbstractChannelHandlerContext context = findContextOutbound();
        ((ChannelOutboundHandler) context.handler()).bind(context, socketAddress, promise);
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise promise) {
        AbstractChannelHandlerContext context = findContextOutbound();
        ((ChannelOutboundHandler) context.handler()).connect(context, socketAddress, promise);
        return promise;
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        AbstractChannelHandlerContext context = findContextOutbound();
        ((ChannelOutboundHandler) context.handler()).write(context, msg, promise);
        return promise;
    }

    @Override
    public ChannelFuture flush(ChannelPromise promise) {
        AbstractChannelHandlerContext context = findContextOutbound();
        ((ChannelOutboundHandler) context.handler()).flush(context, promise);
        return promise;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        AbstractChannelHandlerContext context = findContextOutbound();
        ((ChannelOutboundHandler) context.handler()).close(context, promise);
        return promise;
    }

    @Override
    public ChannelFuture unregister(ChannelPromise promise) {
        AbstractChannelHandlerContext context = findContextOutbound();
        ((ChannelOutboundHandler) context.handler()).unregister(context, promise);
        return promise;
    }

    private AbstractChannelHandlerContext findContextInbound() {
        AbstractChannelHandlerContext context = this;
        do {
            context = context.next;
        } while ((context.mask & INBOUND_MASK) == 0);
        return context;
    }

    private AbstractChannelHandlerContext findContextOutbound() {
        AbstractChannelHandlerContext context = this;
        do {
            context = context.prev;
        } while ((context.mask & OUTBOUND_MASK) == 0);
        return context;
    }
}

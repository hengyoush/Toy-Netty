package io.yhheng.channel;

import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

public interface ChannelOutboundHandler extends ChannelHandler {
    void bind(ChannelHandlerContext context, SocketAddress socketAddress, ChannelPromise promise);
    void unregister(ChannelHandlerContext context, ChannelPromise promise);
    void connect(ChannelHandlerContext context, SocketAddress socketAddress, ChannelPromise promise);
    void write(ChannelHandlerContext context, Object msg, ChannelPromise promise);
    void read(ChannelHandlerContext context, ChannelPromise promise);
    void flush(ChannelHandlerContext context, ChannelPromise promise);
    void close(ChannelHandlerContext context, ChannelPromise promise);
}

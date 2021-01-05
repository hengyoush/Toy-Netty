package io.yhheng.channel;

public interface ChannelInboundHandler extends ChannelHandler {
    void channelRegistered(ChannelHandlerContext context);
    void channelUnregistered(ChannelHandlerContext context);
    void channelActive(ChannelHandlerContext context);
    void channelInactive(ChannelHandlerContext context);
    void channelRead(ChannelHandlerContext context);
}

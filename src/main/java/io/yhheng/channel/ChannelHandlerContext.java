package io.yhheng.channel;

public interface ChannelHandlerContext extends ChannelInboundInvoker, ChannelOutboundInvoker {
    Channel channel();
    ChannelHandler handler();
    ChannelPipeline pipeline();
}
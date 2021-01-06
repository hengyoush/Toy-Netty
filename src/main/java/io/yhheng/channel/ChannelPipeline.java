package io.yhheng.channel;

public interface ChannelPipeline extends ChannelInboundInvoker, ChannelOutboundInvoker {
    ChannelPipeline addLast(ChannelHandler handler);

    ChannelPipeline fireChannelRegistered();
    ChannelPipeline fireChannelUnRegister();
    ChannelPipeline fireChannelActive();
    ChannelPipeline fireChannelInActive();
    ChannelPipeline fireChannelRead(Object msg);
    ChannelPipeline fireExceptionCaught(Throwable cause);
}

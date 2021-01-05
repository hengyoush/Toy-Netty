package io.yhheng.channel;

public interface ChannelInboundInvoker {
    ChannelInboundInvoker fireChannelRegistered();
    ChannelInboundInvoker fireChannelUnRegister();
    ChannelInboundInvoker fireChannelActive();
    ChannelInboundInvoker fireChannelInActive();
    ChannelInboundInvoker fireChannelRead();
}

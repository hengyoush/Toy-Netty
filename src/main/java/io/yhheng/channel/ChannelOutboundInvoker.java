package io.yhheng.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.yhheng.concurrent.Promise;

import java.net.SocketAddress;

public interface ChannelOutboundInvoker {
    ChannelFuture bind(SocketAddress socketAddress, ChannelPromise promise);
    ChannelFuture connect(SocketAddress socketAddress, ChannelPromise promise);
    ChannelFuture write(Object msg, ChannelPromise promise);
    ChannelFuture flush(ChannelPromise promise);
    ChannelFuture close(ChannelPromise promise);
    ChannelFuture unregister(ChannelPromise promise);
}

package io.yhheng.channel;

import io.yhheng.concurrent.Promise;

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
    public ChannelInboundInvoker fireChannelRead() {
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
    public void bind(SocketAddress socketAddress, Promise<Void> promise) {

    }

    @Override
    public void connect(SocketAddress socketAddress, Promise<Void> promise) {

    }

    @Override
    public void write(Object msg, Promise<Void> promise) {

    }

    @Override
    public void flush(Promise<Void> promise) {

    }

    @Override
    public void close(Promise<Void> promise) {

    }

    @Override
    public void unregister(Promise<Void> promise) {

    }
}

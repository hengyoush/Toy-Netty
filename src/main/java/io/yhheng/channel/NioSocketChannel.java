package io.yhheng.channel;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @version V1.0
 * @author: yhheng
 * @date 2021/1/6
 */
public class NioSocketChannel extends NioByteSocketChannel {
    public NioSocketChannel(SocketChannel javaChannel) {

    }

    @Override
    boolean doConnect(SocketAddress remoteAddress) {
        return false;
    }

    @Override
    void doFinishConnect() {

    }

    @Override
    void doBind(SocketAddress localAddress) throws Exception {

    }

    @Override
    void doRegister() throws Exception {

    }

    @Override
    void beginRead() {

    }

    @Override
    void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {

    }

    @Override
    void doShutdownOutput() throws Exception {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}

package io.yhheng.channel;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/6
 */
public class NioServerSocketChannel extends NioMessageSocketChannel {
    @Override
    boolean doConnect(SocketAddress remoteAddress) {
        return false;
    }

    @Override
    void doFinishConnect() {
    }

    @Override
    void doBind(SocketAddress localAddress) throws Exception {
        ServerSocketChannel socketChannel = javaChannel();
        socketChannel.bind(localAddress);
    }

    @Override
    void doRegister() throws Exception {
        this.selectionKey = javaChannel().register(eventloop().selector(), 0, this);
    }

    @Override
    void beginRead() {
        if (!selectionKey.isValid()) {
            return;
        }

        int interestOps = selectionKey.interestOps();
        selectionKey.interestOps(interestOps | SelectionKey.OP_ACCEPT);
    }

    @Override
    void doShutdownOutput() throws Exception {
        close(voidPromise());
    }

    @Override
    void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {

    }

    @Override
    public boolean isOpen() {
        return javaChannel().isOpen();
    }

    @Override
    public boolean isActive() {
        return isOpen() && javaChannel().socket().isBound();
    }

    @Override
    public ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) super.javaChannel();
    }

    @Override
    int doRead(List<Object> bufs) throws Exception {
        SocketChannel socketChannel = javaChannel().accept();
        if (socketChannel != null) {
            bufs.add(new NioSocketChannel(socketChannel));
            return 1;
        } else {
            return 0;
        }
    }
}

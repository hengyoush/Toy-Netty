package io.yhheng.channel;

import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/6
 */
public class NioSocketChannel extends NioByteSocketChannel {
    public NioSocketChannel(SocketChannel javaChannel) {

    }

    @Override
    boolean doConnect(SocketAddress remoteAddress) throws Exception {
        boolean connect = javaChannel().connect(remoteAddress);
        return connect;
    }

    @Override
    void doFinishConnect() throws Exception {
        javaChannel().finishConnect();
    }

    @Override
    void doBind(SocketAddress localAddress) throws Exception {

    }

    @Override
    void doRegister() throws Exception {
        javaChannel().register(eventloop().selector(), SelectionKey.OP_READ, this);
    }

    @Override
    void beginRead() {

    }

    @Override
    void doWrite(ChannelOutboundBuffer in) throws Exception {
        if (in.isEmpty()) {
            clearOpWrite();
            return;
        }

        ByteBuffer[] byteBuffers = in.nioBuffers(1024, 1024); // TODO
        int nioBufferCount = in.nioBufferCount();
        int writeSpinCount = 16; // TODO
        do {
            switch (nioBufferCount) {
                case 0: {
                    return;
                }
                case 1: {
                    ByteBuffer byteBuffer = byteBuffers[0];
                    int attemptWriteBytes = byteBuffer.remaining();
                    int realWriteBytes = javaChannel().write(byteBuffer);
                    if (realWriteBytes <= 0) {
                        // incompleteWrite TODO
                    }
                    in.removeBytes(realWriteBytes);
                    writeSpinCount--;
                    break;
                }
                default: {
                    // gatheringWrite
                    long realWriteBytes = javaChannel().write(byteBuffers, 0, nioBufferCount);
                    if (realWriteBytes <= 0) {
                        // incompleteWrite TODO
                    }
                    in.removeBytes(realWriteBytes);
                    writeSpinCount--;
                    break;
                }
            }
        } while (writeSpinCount > 0);
    }

    private void clearOpWrite() {
        if (!selectionKey.isValid()) {
            return;
        }

        if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) != 0) {
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }

    @Override
    void doShutdownOutput() throws Exception {
        javaChannel().shutdownOutput();
    }

    @Override
    public boolean isOpen() {
        return javaChannel().isOpen();
    }

    @Override
    public boolean isActive() {
        return isOpen() && javaChannel().isConnected();
    }
}

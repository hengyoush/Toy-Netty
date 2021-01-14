package io.yhheng.channel;

import io.netty.buffer.ByteBufAllocator;
import io.yhheng.buffer.RecvByteBufAllocator;
import io.yhheng.eventloop.EventLoop;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/6
 */
public class NioSocketChannel extends NioByteSocketChannel {
    public NioSocketChannel(SelectableChannel ch,
                            EventLoop eventLoop,
                            ChannelPipeline channelPipeline,
                            ChannelConfig config) {
        super(ch, eventLoop, channelPipeline, config);
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
        if (!selectionKey.isValid()) {
            return;
        }

        int interestOps = selectionKey.interestOps();
        if ((interestOps & SelectionKey.OP_READ) == 0) {
            selectionKey.interestOps(interestOps | SelectionKey.OP_READ);
        }
    }

    @Override
    void doWrite(ChannelOutboundBuffer in) throws Exception {
        if (in.isEmpty()) {
            clearOpWrite();
            return;
        }

        ByteBuffer[] byteBuffers = in.nioBuffers(1024, config().getMaxBytesPerGatheringWrite());
        int nioBufferCount = in.nioBufferCount();
        int writeSpinCount = config().writeSpinCount();
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
                        incompleteWrite(true);
                    }
                    in.removeBytes(realWriteBytes);
                    writeSpinCount--;
                    break;
                }
                default: {
                    // gatheringWrite
                    long realWriteBytes = javaChannel().write(byteBuffers, 0, nioBufferCount);
                    if (realWriteBytes <= 0) {
                        incompleteWrite(true);
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

    private void setOpWrite() {
        if (!selectionKey.isValid()) {
            return;
        }

        if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        }
    }

    private void incompleteWrite(boolean setOpWrite) {
        if (setOpWrite) {
            setOpWrite();
        } else {
            clearOpWrite();
            eventloop().submit(this::flush0);
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

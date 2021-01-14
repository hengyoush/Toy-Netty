package io.yhheng.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.yhheng.buffer.RecvByteBufAllocator;
import io.yhheng.eventloop.EventLoop;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/6
 */
public abstract class NioByteSocketChannel extends AbstractNioSocketChannel {
    private RecvByteBufAllocator allocator;
    private ByteBufAllocator byteBufAllocator;

    NioByteSocketChannel(SelectableChannel ch,
                         EventLoop eventLoop,
                         ChannelPipeline channelPipeline,
                         ChannelConfig config) {
        super(ch, eventLoop, channelPipeline, config);
        this.allocator = config.getRecvByteBufAllocator();
        this.byteBufAllocator = config.getByteBufAllocator();
    }

    @Override
    public void read() {
        // 预测读取字节数
        boolean close = false;
        try {
            do {
                ByteBuf buf = allocator.allocate(byteBufAllocator);
                int attemptReadBytes = buf.writableBytes();
                int readBytes = buf.writeBytes(javaChannel(), attemptReadBytes);
                allocator.recordBytesRead(attemptReadBytes, readBytes);
                if (readBytes <= 0) {
                    buf.release();
                    if (readBytes < 0) {
                        close = true;
                    }
                }
                pipeline().fireChannelRead(buf);
            } while (allocator.continueReading());

            if (close) {
                close(voidPromise());
            }
        } catch (Throwable cause) {
            pipeline().fireExceptionCaught(cause);
            close(voidPromise());
        }

    }

    @Override
    public SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }
}

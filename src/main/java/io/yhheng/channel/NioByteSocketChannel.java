package io.yhheng.channel;

import io.netty.buffer.ByteBuf;
import io.yhheng.buffer.RecvByteBufAllocator;

import java.nio.channels.SocketChannel;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/6
 */
public abstract class NioByteSocketChannel extends AbstractNioSocketChannel {
    private RecvByteBufAllocator allocator;

    @Override
    public void read() {
        // 预测读取字节数
        boolean close = false;
        try {
            do {
                ByteBuf buf = allocator.allocate();
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

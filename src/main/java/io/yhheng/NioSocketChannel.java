package com.iflytek.edu;

import com.iflytek.edu.alloc.RecvByteBufAllocator;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/4
 */
public class NioSocketChannel {
    private SocketChannel socketChannel;
    private RecvByteBufAllocator allocator;

    public void read() throws Exception {
        // 预测读取字节数
        boolean close = false;
        do {
            ByteBuf buf = allocator.allocate();
            int attemptReadBytes = buf.writableBytes();
            int readBytes = buf.writeBytes(socketChannel, attemptReadBytes);
            allocator.recordBytesRead(attemptReadBytes, readBytes);
            if (readBytes <= 0) {
                buf.release();
                if (readBytes < 0) {
                    close = true;
                }
            }
            // TODO fire channel read
        } while (allocator.continueReading());

        if (close) {
            // TODO close
        }
    }
    private List<Object> readBuf = new ArrayList<>();
    public void readByMessage() {
        boolean close = false;
        Throwable exception = null;
        try {
            do {
                int messageNum = doRead(readBuf);
                if (messageNum == 0) {
                    break;
                }

                if (messageNum < 0) {
                    close = true;
                    break;
                }
                allocator.recordMessageRead(messageNum);
            } while (allocator.continueReading());
        } catch (Throwable t) {
            exception = t;
        }


        for (Object buf : readBuf) {
            // TODO fire pipeline read
        }

        readBuf.clear();
        if (exception != null) {
            close = closeOnReadError(exception);
            // TODO fire exception?
        }

        if (close) {
            // TODO do close
        }
    }

    private boolean closeOnReadError(Throwable t) {
        if (!isActive()) {
            return true;
        }

        if (t instanceof IOException) {
            return true;
        }
        return true;
    }

    private int doRead(List<Object> readBuf) {
        return 0;
    }

    private boolean isActive() {
        return false;
    }
}

package io.yhheng.channel;

import io.yhheng.buffer.RecvByteBufAllocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @author yhheng
 * @date 2021/1/6
 */
public abstract class NioMessageSocketChannel extends AbstractNioSocketChannel {
    private List<Object> readBuf = new ArrayList<>();
    private RecvByteBufAllocator allocator;

    @Override
    public void read() {
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
            pipeline().fireChannelRead(buf);
        }

        readBuf.clear();
        if (exception != null) {
            close = closeOnReadError(exception);
            pipeline().fireExceptionCaught(exception);
        }

        if (close) {
            close(voidPromise());
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

    abstract int doRead(List<Object> bufs) throws Exception;
}

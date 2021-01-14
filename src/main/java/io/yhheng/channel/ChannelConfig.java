package io.yhheng.channel;

import io.netty.buffer.ByteBufAllocator;
import io.yhheng.buffer.RecvByteBufAllocator;

public interface ChannelConfig {
    int connectTimeoutMills();

    /**
     * 在写发生错误的时候是否关闭连接
     * @return
     */
    boolean isAutoClose();

    int writeSpinCount();

    int getMaxBytesPerGatheringWrite();

    RecvByteBufAllocator getRecvByteBufAllocator();

    ByteBufAllocator getByteBufAllocator();
}

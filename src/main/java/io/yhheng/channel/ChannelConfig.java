package io.yhheng.channel;

public interface ChannelConfig {
    int connectTimeoutMills();

    /**
     * 在写发生错误的时候是否关闭连接
     * @return
     */
    boolean isAutoClose();
}

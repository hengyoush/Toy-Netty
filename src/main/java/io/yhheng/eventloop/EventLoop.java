package io.yhheng.eventloop;


import io.yhheng.channel.Channel;
import io.yhheng.concurrent.Future;

import java.nio.channels.Selector;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author yhheng
 * @date 2020/12/30
 */
public interface EventLoop {
    void run();
    void shutdown();

    Future<Void> register(Channel channel);
    Future<?> schedule(Runnable command, int delay, TimeUnit timeunit);
    Future<?> submit(Runnable command);

    boolean inEventLoop();
    Selector selector();
}

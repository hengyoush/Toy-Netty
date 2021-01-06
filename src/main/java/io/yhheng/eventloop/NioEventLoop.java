package io.yhheng.eventloop;


import io.yhheng.channel.Channel;
import io.yhheng.concurrent.DefaultPromise;
import io.yhheng.concurrent.Future;
import io.yhheng.concurrent.Promise;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yhheng
 * @version V1.0
 * @date 2020/12/30
 */
public class NioEventLoop extends Thread implements EventLoop {
    private Selector selector;
    private Queue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    /**
     * example:
     * ioRatio=50,ioTime=6,non-ioTime=6
     * ioRatio=30,ioTime=3,non-ioTime=7
     * (100 - ioRatio)/ioRatio * ioTime
     */
    private int ioRatio;
    private volatile boolean isShutdown;

    private Promise<Void> voidPromise = new DefaultPromise<>();

    @Override
    public void run() {
        for (; ; ) {
            try {
                int select = selector.select(1000);
                if (ioRatio == 100) {
                    try {
                        processSelectedKeys();
                    } finally {
                        runAllTasks();
                    }
                } else if (select > 0) {
                    long startTime = System.nanoTime();
                    try {
                        processSelectedKeys();
                    } finally {
                        long ioTime = System.nanoTime() - startTime;
                        runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                    }
                } else {
                    runAllTasks();
                }
            } catch (Exception e) {
                // TODO handle exception
            }

            if (isShutdown) {
                closeAll();
            }
        }
    }

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @Override
    public Future<Void> register(Channel channel) {
        Promise<Void> promise = new DefaultPromise<>();
        channel.register(this, promise);
        return promise;
    }

    @Override
    public Future<?> schedule(Runnable command, int delay, TimeUnit timeunit) {
        return null;
    }

    @Override
    public Future<?> submit(Runnable command) {
        taskQueue.offer(command);
        DefaultPromise<Void> promise = new DefaultPromise<>();
        promise.setSuccess(null);
        return promise;
    }

    @Override
    public boolean inEventLoop() {
        return this == Thread.currentThread();
    }

    @Override
    public Selector selector() {
        return selector;
    }

    private void closeAll() {
        Set<SelectionKey> keys = selector.keys();
        List<Channel> channels =
                keys.stream().map(SelectionKey::attachment).map(Channel.class::cast).collect(Collectors.toList());
        channels.forEach(c -> c.close(voidPromise));
    }

    private void runAllTasks(long timeout) {
        long finalTime = System.currentTimeMillis() + timeout;
        if (taskQueue.isEmpty()) {
            return;
        }
        while (true) {
            Runnable command = taskQueue.poll();
            try {
                if (command != null) {
                    command.run();
                }
            } catch (Throwable e) {
                // TODO
            } finally {
                if (System.currentTimeMillis() > finalTime) {
                    break;
                }
            }
        }
    }

    private void runAllTasks() {
        runAllTasks(0L);
    }

    private void processSelectedKeys() {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        for (SelectionKey key : selectedKeys) {
            Channel channel = (Channel) key.attachment();
            if (!key.isValid()) {
                // 关闭channel
                if (channel.eventloop() == this) {
                    channel.close(voidPromise);
                }
            }

            try {
                int readyOps = key.readyOps();
                if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
                    int interestOps = key.interestOps();
                    interestOps &= ~SelectionKey.OP_CONNECT;
                    key.interestOps(interestOps);
                }

                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    channel.flush(voidPromise);
                }

                if ((readyOps & SelectionKey.OP_READ) != 0) {
                    channel.read();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                channel.close(voidPromise);
            }
        }
    }
}

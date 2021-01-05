package io.yhheng.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author yhheng
 * @date 2020/12/30
 */
public interface Future<V> extends java.util.concurrent.Future<V> {
    boolean isSuccess();
    Throwable cause();
    Future<V> addListener(FutureListener<? extends Future<V>> listener);
    Future<V> sync() throws InterruptedException;
    Future<V> await() throws InterruptedException;
    boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException;

}

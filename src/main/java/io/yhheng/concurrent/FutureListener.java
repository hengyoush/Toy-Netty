package io.yhheng.concurrent;

public interface FutureListener<F extends Future<?>> {
    void operatorComplete(F future) throws Exception;
}

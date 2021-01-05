package io.yhheng.concurrent;

public interface Promise<V> extends Future<V> {
    Promise<V> setSuccess(V result);
    Promise<V> setFail(Throwable cause);
    boolean tryFail(Throwable cause);
    boolean trySuccess(V result);
}

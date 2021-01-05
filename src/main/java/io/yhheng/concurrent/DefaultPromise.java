package io.yhheng.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class DefaultPromise<T> implements Promise<T> {
    private volatile Object result;
    private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
    private static final Object CANCEL_RESULT_HOLDER = new Object();
    private static final Object SUCCESS = new Object();
    private List<FutureListener<?>> listeners = new ArrayList<>(1);
    private boolean notifyingListeners;

    @Override
    public boolean isSuccess() {
        return isDone() && !(result instanceof CauseHolder);
    }

    @Override
    public Throwable cause() {
        if (!isDone()) {
            return null;
        }

        if (result instanceof CauseHolder) {
            return ((CauseHolder) result).cause;
        } else {
            return null;
        }
    }

    @Override
    public Future<T> addListener(FutureListener<? extends Future<T>> listener) {
        synchronized (this) {
            listeners.add(listener);
        }
        return this;
    }

    @Override
    public Future<T> sync() throws InterruptedException  {
        await();
        if (cause() != null) {
            throw (InterruptedException) cause(); // TODO 可能发生cast强转错误，有没有更好的方式
        }
        return this;
    }

    @Override
    public Future<T> await() throws InterruptedException {
        if (isDone()) {
            return this;
        }

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        boolean interrupted = false;
        synchronized (this) {
            while (!isDone()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return await0(timeUnit.toNanos(timeout), true);
    }

    private boolean await0(long nano, boolean interruptible) throws InterruptedException {
        if (isDone()) {
            return true;
        }

        if (interruptible && Thread.interrupted()) {
            throw new InterruptedException();
        }

        boolean interrupted = false;
        long startNano = System.nanoTime();
        synchronized (this) {
            while (!isDone()) {
                try {
                    wait(nano / 1000000, (int) (nano % 1000000));
                } catch (InterruptedException e) {
                    if (interruptible) {
                        throw e;
                    } else {
                        interrupted = true;
                    }

                } finally {
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (System.nanoTime() - startNano >= nano) {
                    return isDone();
                }
            }
            return true;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (RESULT_UPDATER.compareAndSet(this, null, CANCEL_RESULT_HOLDER)) {
            notifyListeners();
            return true;
        }
        return false;
    }

    private void notifyListeners() {
        synchronized (this) {
             if (notifyingListeners || listeners == null) {
                 return;
             }

             notifyingListeners = true;
        }

        for (FutureListener listener : listeners) {
            try {
                listener.operatorComplete(this);
            } catch (Exception e) {
                // TODO logger
            }
        }

        synchronized (this) {
            if (notifyingListeners) {
                notifyingListeners = false;
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return result == CANCEL_RESULT_HOLDER;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        await();
        return getNow();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        await(timeout, unit);
        return getNow();
    }

    private T getNow() throws InterruptedException, ExecutionException {
        if (cause() != null ) {
            throw new ExecutionException(cause());
        }

        if (result == SUCCESS) {
            return null;
        }

        return (T) result;
    }

    @Override
    public Promise<T> setSuccess(T result) {
        if (setValue0(result == null ? SUCCESS : result)) {
            return this;
        } else {
            throw new IllegalStateException("already complete!");
        }
    }

    @Override
    public Promise<T> setFail(Throwable cause) {
        if (setValue0(new CauseHolder(cause))) {
            return this;
        } else {
            throw new IllegalStateException("already complete!");
        }
    }

    @Override
    public boolean tryFail(Throwable cause) {
        return setValue0(new CauseHolder(cause));
    }

    @Override
    public boolean trySuccess(T result) {
        return setValue0(result == null ? SUCCESS : result);
    }

    private boolean setValue0(Object result) {
        if (RESULT_UPDATER.compareAndSet(this, null, result)) {
            notifyListeners();
            return true;
        }

        return false;
    }

    private static final class CauseHolder {
        final Throwable cause;
        CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }
}

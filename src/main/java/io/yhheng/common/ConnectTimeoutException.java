package io.yhheng.common;

public class ConnectTimeoutException extends Exception {
    public ConnectTimeoutException() {
    }

    public ConnectTimeoutException(String message) {
        super(message);
    }

    public ConnectTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectTimeoutException(Throwable cause) {
        super(cause);
    }

    public ConnectTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

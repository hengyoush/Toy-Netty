package io.yhheng.common;

public class ExtendedClosedChannelException extends Exception {
    public ExtendedClosedChannelException() {
    }

    public ExtendedClosedChannelException(String message) {
        super(message);
    }

    public ExtendedClosedChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtendedClosedChannelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ExtendedClosedChannelException(Throwable e) {
        super(e);


    }
}

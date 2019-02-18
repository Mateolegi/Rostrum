package com.mateolegi.rostrum.exception;

public class NoIdFoundException extends RuntimeException {

    public NoIdFoundException() {
    }

    public NoIdFoundException(String message) {
        super(message);
    }

    public NoIdFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoIdFoundException(Throwable cause) {
        super(cause);
    }

    public NoIdFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

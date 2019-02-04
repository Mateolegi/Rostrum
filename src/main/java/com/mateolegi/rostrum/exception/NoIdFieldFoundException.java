package com.mateolegi.rostrum.exception;

public class NoIdFieldFoundException extends RuntimeException {

    public NoIdFieldFoundException() {
    }

    public NoIdFieldFoundException(String message) {
        super(message);
    }

    public NoIdFieldFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoIdFieldFoundException(Throwable cause) {
        super(cause);
    }

    public NoIdFieldFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

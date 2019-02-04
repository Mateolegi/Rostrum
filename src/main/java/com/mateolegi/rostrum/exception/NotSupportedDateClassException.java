package com.mateolegi.rostrum.exception;

public class NotSupportedDateClassException extends RuntimeException {

    public NotSupportedDateClassException() {
    }

    public NotSupportedDateClassException(String message) {
        super(message);
    }

    public NotSupportedDateClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportedDateClassException(Throwable cause) {
        super(cause);
    }

    public NotSupportedDateClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

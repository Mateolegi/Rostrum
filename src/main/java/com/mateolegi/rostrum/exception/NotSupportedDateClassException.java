package com.mateolegi.rostrum.exception;

public class NotSupportedDateClassException extends RuntimeException {

    public NotSupportedDateClassException(String message) {
        super(message);
    }

    public NotSupportedDateClassException(String message, Throwable cause) {
        super(message, cause);
    }
}

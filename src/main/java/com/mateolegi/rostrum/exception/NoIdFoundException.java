package com.mateolegi.rostrum.exception;

public class NoIdFoundException extends RuntimeException {

    public NoIdFoundException(String message) {
        super(message);
    }

    public NoIdFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

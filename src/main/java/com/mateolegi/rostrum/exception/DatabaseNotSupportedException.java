package com.mateolegi.rostrum.exception;

public class DatabaseNotSupportedException extends RuntimeException {

    public DatabaseNotSupportedException(String message) {
        super(message);
    }
}

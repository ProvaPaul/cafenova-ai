package com.smartcafe.exception;

/**
 * Root unchecked exception for the application.
 * Using RuntimeException so the DAO/Service layers don't need to declare
 * checked exceptions in every method signature, keeping interfaces clean.
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}

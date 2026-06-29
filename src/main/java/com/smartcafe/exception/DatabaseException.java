package com.smartcafe.exception;

/**
 * Wraps any SQLException that escapes the DAO layer.
 * Service and Controller code catches AppException; if they need to
 * distinguish DB errors from auth errors they can catch this subtype.
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

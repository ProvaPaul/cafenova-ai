package com.smartcafe.exception;

/**
 * Thrown by ValidationUtil or Service methods when user-supplied data
 * fails field-level checks (blank required field, bad email format, etc.)
 * Kept separate from AuthException so callers can display validation
 * errors differently (inline field errors vs. modal dialog).
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message);
    }
}

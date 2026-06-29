package com.smartcafe.exception;

/**
 * Thrown when authentication or authorisation fails:
 * bad credentials, deactivated account, duplicate username, etc.
 * The UI catches this and displays the message directly to the user,
 * so messages must be human-readable (not stack-trace language).
 */
public class AuthException extends AppException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

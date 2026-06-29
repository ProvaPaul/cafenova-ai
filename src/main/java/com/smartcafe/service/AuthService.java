package com.smartcafe.service;

import com.smartcafe.model.Role;
import com.smartcafe.model.User;

/**
 * Authentication and account-management contract.
 * Implementations handle all business rules; the DAO is responsible only for
 * SQL.  The Controller calls these methods and relays results to the View.
 */
public interface AuthService {

    /**
     * Validates credentials and returns the authenticated {@link User}.
     *
     * @param identifier username OR email (caller passes whichever the user typed)
     * @param password   plaintext — compared against the stored BCrypt hash
     * @throws com.smartcafe.exception.AuthException       on bad credentials or inactive account
     * @throws com.smartcafe.exception.ValidationException on blank inputs
     */
    User login(String identifier, String password);

    /**
     * Creates a new staff account.  The first account ever created is always
     * promoted to ADMIN regardless of the {@code role} parameter.
     *
     * @throws com.smartcafe.exception.ValidationException on format errors
     * @throws com.smartcafe.exception.AuthException       on duplicate username/email
     */
    User signup(String fullName, String username, String email, String phone,
                String password, String confirmPassword, Role role);

    /**
     * Generates a secure reset token, persists it (1-hour TTL), and returns it.
     * In production this token would be emailed; for now the UI displays it.
     *
     * @throws com.smartcafe.exception.AuthException if the email is not found
     */
    String initiatePasswordReset(String email);

    /**
     * Verifies the token and replaces the user's password.
     *
     * @throws com.smartcafe.exception.AuthException       if the token is invalid/expired
     * @throws com.smartcafe.exception.ValidationException if the passwords don't match
     */
    void resetPassword(String token, String newPassword, String confirmPassword);

    /** {@code true} when the {@code users} table is empty — used to auto-assign Admin. */
    boolean isFirstUser();
}

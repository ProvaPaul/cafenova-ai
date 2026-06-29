package com.smartcafe.dao;

import com.smartcafe.model.PasswordResetToken;
import com.smartcafe.model.User;

import java.util.Optional;

/**
 * Data-access contract for the {@code users} and {@code password_reset_tokens}
 * tables.  All implementations must use PreparedStatements — never string
 * concatenation for SQL parameters.
 */
public interface UserDao {

    Optional<User> findById(int id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /** Single query that searches both username and email columns. */
    Optional<User> findByUsernameOrEmail(String identifier);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Inserts the user and populates {@code user.id} with the generated key. */
    User save(User user);

    void updatePassword(int userId, String newPasswordHash);

    /** Total number of user rows — used to detect first-run / empty database. */
    long count();

    // ── Password Reset ────────────────────────────────────────────────────────

    void saveResetToken(PasswordResetToken token);

    /** Returns a token only if it is unused AND not yet expired. */
    Optional<PasswordResetToken> findValidResetToken(String token);

    void markTokenUsed(int tokenId);
}

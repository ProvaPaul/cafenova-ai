package com.smartcafe.service.impl;

import com.smartcafe.dao.UserDao;
import com.smartcafe.exception.AuthException;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.PasswordResetToken;
import com.smartcafe.model.Role;
import com.smartcafe.model.User;
import com.smartcafe.service.AuthService;
import com.smartcafe.util.PasswordUtil;
import com.smartcafe.util.TokenUtil;
import com.smartcafe.util.ValidationUtil;

import java.time.LocalDateTime;

public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public User login(String identifier, String password) {
        ValidationUtil.requireNonBlank(identifier, "Username or email");
        ValidationUtil.requireNonBlank(password,   "Password");

        User user = userDao.findByUsernameOrEmail(identifier.trim())
                .orElseThrow(() -> new AuthException("Invalid username/email or password"));

        if (!user.isActive()) {
            throw new AuthException("Your account has been deactivated. Please contact Admin.");
        }

        // BCrypt.checkpw is intentionally slow — do NOT short-circuit before it
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new AuthException("Invalid username/email or password");
        }

        return user;
    }

    // ── Signup ────────────────────────────────────────────────────────────────

    @Override
    public User signup(String fullName, String username, String email, String phone,
                       String password, String confirmPassword, Role role) {

        ValidationUtil.requireNonBlank(fullName,        "Full name");
        ValidationUtil.requireNonBlank(username,        "Username");
        ValidationUtil.requireNonBlank(email,           "Email");
        ValidationUtil.requireNonBlank(password,        "Password");
        ValidationUtil.requireNonBlank(confirmPassword, "Confirm password");

        if (!ValidationUtil.isValidEmail(email.trim())) {
            throw new ValidationException("Please enter a valid email address");
        }
        if (!ValidationUtil.isValidUsername(username.trim())) {
            throw new ValidationException(
                    "Username must be 3-50 characters and contain only letters, digits, or underscores");
        }
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }

        String normalUsername = username.trim().toLowerCase();
        String normalEmail    = email.trim().toLowerCase();

        if (userDao.existsByUsername(normalUsername)) {
            throw new AuthException("Username '" + username.trim() + "' is already taken");
        }
        if (userDao.existsByEmail(normalEmail)) {
            throw new AuthException("An account with that email address already exists");
        }

        // First account ever created → always Admin (bootstrap scenario)
        Role assignedRole = isFirstUser() ? Role.ADMIN : role;

        User user = new User(
                fullName.trim(),
                normalUsername,
                normalEmail,
                PasswordUtil.hash(password),
                (phone != null && !phone.isBlank()) ? phone.trim() : null,
                assignedRole
        );

        return userDao.save(user);
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    @Override
    public String initiatePasswordReset(String email) {
        ValidationUtil.requireNonBlank(email, "Email");
        if (!ValidationUtil.isValidEmail(email.trim())) {
            throw new ValidationException("Please enter a valid email address");
        }

        User user = userDao.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AuthException(
                        "No account found with that email address"));

        if (!user.isActive()) {
            throw new AuthException("This account is deactivated");
        }

        String token      = TokenUtil.generateToken();
        LocalDateTime exp = LocalDateTime.now().plusHours(1);

        userDao.saveResetToken(new PasswordResetToken(user.getId(), token, exp));

        // In production: send token via email (SMTP / SendGrid / etc.)
        // For this desktop app we return it so the UI can display it.
        return token;
    }

    @Override
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        ValidationUtil.requireNonBlank(token,           "Reset token");
        ValidationUtil.requireNonBlank(newPassword,     "New password");
        ValidationUtil.requireNonBlank(confirmPassword, "Confirm password");

        if (newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }

        PasswordResetToken prt = userDao.findValidResetToken(token.trim())
                .orElseThrow(() -> new AuthException(
                        "Reset token is invalid or has expired. Please request a new one."));

        userDao.updatePassword(prt.getUserId(), PasswordUtil.hash(newPassword));
        userDao.markTokenUsed(prt.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Override
    public boolean isFirstUser() {
        return userDao.count() == 0;
    }
}

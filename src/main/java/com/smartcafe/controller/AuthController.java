package com.smartcafe.controller;

import com.smartcafe.exception.AppException;
import com.smartcafe.model.Role;
import com.smartcafe.model.User;
import com.smartcafe.service.AuthService;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.MainFrame;

/**
 * Thin coordinator between the auth View panels and AuthService.
 *
 * Rules:
 *  - No business logic here — that belongs in the Service.
 *  - No SQL here — that belongs in the DAO.
 *  - Always calls mainFrame on the EDT (the View dispatches events on EDT,
 *    so the controller methods are already on EDT when invoked from listeners).
 */
public class AuthController {

    private final AuthService authService;
    private final MainFrame   mainFrame;

    public AuthController(AuthService authService, MainFrame mainFrame) {
        this.authService = authService;
        this.mainFrame   = mainFrame;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public void login(String identifier, String password) {
        try {
            User user = authService.login(identifier, password);
            SessionManager.setCurrentUser(user);
            mainFrame.navigateToDashboard(user.getRole());
        } catch (AppException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    // ── Signup ────────────────────────────────────────────────────────────────

    public void signup(String fullName, String username, String email, String phone,
                       String password, String confirmPassword, Role role) {
        try {
            authService.signup(fullName, username, email, phone, password, confirmPassword, role);
            mainFrame.showSuccess("Account created successfully. Please log in.");
            mainFrame.showPanel(MainFrame.PANEL_LOGIN);
        } catch (AppException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    /**
     * Returns the reset token so the UI can display it to the user.
     * Returns {@code null} if the service threw (error already shown via mainFrame).
     */
    public String initiatePasswordReset(String email) {
        try {
            return authService.initiatePasswordReset(email);
        } catch (AppException e) {
            mainFrame.showError(e.getMessage());
            return null;
        }
    }

    /**
     * Returns {@code true} on success so the UI can advance to the confirmation step.
     */
    public boolean resetPassword(String token, String newPassword, String confirmPassword) {
        try {
            authService.resetPassword(token, newPassword, confirmPassword);
            return true;
        } catch (AppException e) {
            mainFrame.showError(e.getMessage());
            return false;
        }
    }

    // ── Navigation Helpers ────────────────────────────────────────────────────

    public void logout() {
        SessionManager.clearCurrentUser();
        mainFrame.showPanel(MainFrame.PANEL_LOGIN);
    }

    public void showLogin()          { mainFrame.showPanel(MainFrame.PANEL_LOGIN); }
    public void showSignup()         { mainFrame.showPanel(MainFrame.PANEL_SIGNUP); }
    public void showForgotPassword() { mainFrame.showPanel(MainFrame.PANEL_FORGOT); }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** {@code true} when no users exist yet — used by SignupPanel to auto-assign Admin. */
    public boolean isFirstTimeSetup() {
        return authService.isFirstUser();
    }
}

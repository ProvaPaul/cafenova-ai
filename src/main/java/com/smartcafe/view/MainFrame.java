package com.smartcafe.view;

import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.model.Role;
import com.smartcafe.view.auth.ForgotPasswordPanel;
import com.smartcafe.view.auth.LoginPanel;
import com.smartcafe.view.auth.SignupPanel;
import com.smartcafe.view.dashboard.AdminDashboard;
import com.smartcafe.view.dashboard.CashierDashboard;
import com.smartcafe.view.dashboard.KitchenDashboard;
import com.smartcafe.view.dashboard.ManagerDashboard;

import javax.swing.*;
import java.awt.*;

/**
 * Root application window.
 *
 * Uses CardLayout to switch between all top-level panels without disposing
 * and recreating the JFrame.  Panels are registered under string keys defined
 * as public constants so other classes can request navigation without
 * hard-coding strings.
 *
 * Wiring order:
 *   1. MainFrame is constructed (empty, no controller yet)
 *   2. Caller injects AuthController via setController()
 *   3. Caller calls init() to build and display all panels
 */
public class MainFrame extends JFrame {

    // Panel keys
    public static final String PANEL_LOGIN    = "LOGIN";
    public static final String PANEL_SIGNUP   = "SIGNUP";
    public static final String PANEL_FORGOT   = "FORGOT";
    public static final String PANEL_ADMIN    = "ADMIN";
    public static final String PANEL_MANAGER  = "MANAGER";
    public static final String PANEL_CASHIER  = "CASHIER";
    public static final String PANEL_KITCHEN  = "KITCHEN";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPane   = new JPanel(cardLayout);

    private AuthController controller;

    // Kept as fields so we can call refresh methods on them
    private LoginPanel          loginPanel;
    private SignupPanel         signupPanel;
    private ForgotPasswordPanel forgotPanel;

    public MainFrame() {
        setTitle(AppConfig.APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        cardPane.setBackground(AppConfig.COLOR_BG);
        setContentPane(cardPane);
    }

    /**
     * Must be called after the controller is injected.
     * Constructs every panel once and registers them with CardLayout.
     */
    public void init() {
        loginPanel    = new LoginPanel(controller);
        signupPanel   = new SignupPanel(controller);
        forgotPanel   = new ForgotPasswordPanel(controller);

        cardPane.add(loginPanel,  PANEL_LOGIN);
        cardPane.add(signupPanel, PANEL_SIGNUP);
        cardPane.add(forgotPanel, PANEL_FORGOT);

        // Dashboards — each gets the controller for logout navigation
        cardPane.add(new AdminDashboard(controller),   PANEL_ADMIN);
        cardPane.add(new ManagerDashboard(controller), PANEL_MANAGER);
        cardPane.add(new CashierDashboard(controller), PANEL_CASHIER);
        cardPane.add(new KitchenDashboard(controller), PANEL_KITCHEN);

        showPanel(PANEL_LOGIN);
        setVisible(true);
    }

    public void setController(AuthController controller) {
        this.controller = controller;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public void showPanel(String key) {
        // Refresh panels that have dynamic state before showing them
        if (PANEL_SIGNUP.equals(key)) {
            signupPanel.refresh();
        }
        if (PANEL_LOGIN.equals(key)) {
            loginPanel.resetForm();
        }
        cardLayout.show(cardPane, key);
    }

    /**
     * Routes the authenticated user to the dashboard that matches their role.
     * Dashboards for other roles are not accessible after this call because
     * only one card is visible at a time and no navigation links cross roles.
     */
    public void navigateToDashboard(Role role) {
        String panel = switch (role) {
            case ADMIN         -> PANEL_ADMIN;
            case MANAGER       -> PANEL_MANAGER;
            case CASHIER       -> PANEL_CASHIER;
            case KITCHEN_STAFF -> PANEL_KITCHEN;
        };
        showPanel(panel);
    }

    // ── Feedback dialogs ──────────────────────────────────────────────────────

    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

package com.smartcafe.view.auth;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Three-step password-reset wizard.
 *
 *  Step 1: User enters their email → a token is generated and shown on screen
 *          (in production this would be emailed; the token dialog simulates that).
 *  Step 2: User enters the token and their new password.
 *  Step 3: Success confirmation with a link back to login.
 *
 * CardLayout drives the step transitions within a single panel instance.
 */
public class ForgotPasswordPanel extends JPanel {

    private static final String STEP_EMAIL   = "STEP_EMAIL";
    private static final String STEP_RESET   = "STEP_RESET";
    private static final String STEP_SUCCESS = "STEP_SUCCESS";

    private final AuthController controller;
    private final CardLayout     cardLayout = new CardLayout();
    private final JPanel         stepCards  = new JPanel(cardLayout);

    // Step 1 fields
    private JTextField emailField;

    // Step 2 fields
    private JTextField     tokenField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ForgotPasswordPanel(AuthController controller) {
        this.controller = controller;
        setBackground(AppConfig.COLOR_BG);
        setLayout(new GridBagLayout());

        stepCards.setOpaque(false);
        stepCards.add(buildStep1(), STEP_EMAIL);
        stepCards.add(buildStep2(), STEP_RESET);
        stepCards.add(buildStep3(), STEP_SUCCESS);

        add(stepCards);
    }

    // ── Step 1: Enter email ───────────────────────────────────────────────────

    private JPanel buildStep1() {
        JPanel form = wizardCard();

        JLabel icon    = emoji("📧", 48);
        JLabel title   = heading("Forgot Password?");
        JLabel sub     = muted("Enter the email address linked to your account and we'll send a reset code.");

        emailField = textField("your-email@example.com");

        RoundedButton sendBtn = new RoundedButton("SEND RESET CODE", RoundedButton.Style.PRIMARY);
        sendBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT + 4));
        sendBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendBtn.addActionListener(e -> onSendCode());

        JPanel backRow = backToLoginRow();

        form.add(icon);
        form.add(Box.createVerticalStrut(12));
        form.add(title);
        form.add(Box.createVerticalStrut(8));
        form.add(sub);
        form.add(Box.createVerticalStrut(28));
        form.add(styledLabel("Email Address"));
        form.add(Box.createVerticalStrut(6));
        form.add(emailField);
        form.add(Box.createVerticalStrut(24));
        form.add(sendBtn);
        form.add(Box.createVerticalStrut(20));
        form.add(backRow);

        return centered(form);
    }

    // ── Step 2: Enter token + new password ───────────────────────────────────

    private JPanel buildStep2() {
        JPanel form = wizardCard();

        JLabel icon  = emoji("🔑", 48);
        JLabel title = heading("Enter Reset Code");
        JLabel sub   = muted("Paste the code that was shown to you and choose a new password.");

        tokenField           = textField("Paste your reset code here");
        newPasswordField     = pwField("New password (min. 8 characters)");
        confirmPasswordField = pwField("Confirm new password");

        RoundedButton resetBtn = new RoundedButton("RESET PASSWORD", RoundedButton.Style.PRIMARY);
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT + 4));
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.addActionListener(e -> onResetPassword());

        RoundedButton backBtn = new RoundedButton("← Back", RoundedButton.Style.GHOST);
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> cardLayout.show(stepCards, STEP_EMAIL));

        form.add(icon);
        form.add(Box.createVerticalStrut(12));
        form.add(title);
        form.add(Box.createVerticalStrut(8));
        form.add(sub);
        form.add(Box.createVerticalStrut(28));
        form.add(styledLabel("Reset Code"));
        form.add(Box.createVerticalStrut(6));
        form.add(tokenField);
        form.add(Box.createVerticalStrut(16));
        form.add(styledLabel("New Password"));
        form.add(Box.createVerticalStrut(6));
        form.add(newPasswordField);
        form.add(Box.createVerticalStrut(16));
        form.add(styledLabel("Confirm Password"));
        form.add(Box.createVerticalStrut(6));
        form.add(confirmPasswordField);
        form.add(Box.createVerticalStrut(24));
        form.add(resetBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(backBtn);

        return centered(form);
    }

    // ── Step 3: Success ───────────────────────────────────────────────────────

    private JPanel buildStep3() {
        JPanel form = wizardCard();

        JLabel icon    = emoji("✅", 56);
        JLabel title   = heading("Password Reset!");
        JLabel sub     = muted("Your password has been updated successfully.\nYou can now sign in with your new password.");

        RoundedButton loginBtn = new RoundedButton("GO TO SIGN IN", RoundedButton.Style.SUCCESS);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT + 4));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            resetWizard();
            controller.showLogin();
        });

        form.add(icon);
        form.add(Box.createVerticalStrut(16));
        form.add(title);
        form.add(Box.createVerticalStrut(10));
        form.add(sub);
        form.add(Box.createVerticalStrut(36));
        form.add(loginBtn);

        return centered(form);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void onSendCode() {
        String email = emailField.getText().trim();
        String token = controller.initiatePasswordReset(email);
        if (token != null) {
            // In production, email the token. Here we display it.
            JOptionPane.showMessageDialog(this,
                    "<html><b>Reset Code (development mode):</b><br/><br/>"
                    + "<code>" + token + "</code><br/><br/>"
                    + "<small>In production this would be sent to your email.</small></html>",
                    "Reset Code Generated",
                    JOptionPane.INFORMATION_MESSAGE);
            tokenField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            cardLayout.show(stepCards, STEP_RESET);
        }
    }

    private void onResetPassword() {
        boolean ok = controller.resetPassword(
                tokenField.getText().trim(),
                new String(newPasswordField.getPassword()),
                new String(confirmPasswordField.getPassword())
        );
        if (ok) {
            cardLayout.show(stepCards, STEP_SUCCESS);
        }
    }

    private void resetWizard() {
        emailField.setText("");
        tokenField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        cardLayout.show(stepCards, STEP_EMAIL);
    }

    // ── Layout Helpers ────────────────────────────────────────────────────────

    /** A blank BoxLayout form card. */
    private static JPanel wizardCard() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /** Centre {@code content} on a full-screen panel. */
    private static JPanel centered(JPanel content) {
        content.setBorder(new EmptyBorder(0, 30, 0, 30));
        content.setMaximumSize(new Dimension(440, Integer.MAX_VALUE));
        content.setPreferredSize(new Dimension(440, 600));

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.add(content);
        return outer;
    }

    private static JPanel backToLoginRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("Remember it?  ");
        lbl.setFont(AppConfig.FONT_BODY);
        lbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JButton btn = linkButton("Sign In");
        // controller.showLogin() attached at site where we have the controller reference
        row.add(lbl);
        row.add(btn);
        btn.addActionListener(e -> {
            // walk up to ForgotPasswordPanel to call controller
            Container c = btn.getParent();
            while (c != null && !(c instanceof ForgotPasswordPanel)) c = c.getParent();
            if (c instanceof ForgotPasswordPanel fp) fp.controller.showLogin();
        });
        return row;
    }

    private static JLabel emoji(String sym, int size) {
        JLabel l = new JLabel(sym);
        l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_TITLE);
        l.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel muted(String text) {
        JLabel l = new JLabel("<html>" + text.replace("\n", "<br/>") + "</html>");
        l.setFont(AppConfig.FONT_SUBTITLE);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        l.setMaximumSize(new Dimension(400, 80));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_LABEL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JTextField textField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private static JPasswordField pwField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.putClientProperty("JPasswordField.showRevealButton", true);
        f.setFont(AppConfig.FONT_BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(AppConfig.FONT_BODY.deriveFont(Font.BOLD));
        b.setForeground(AppConfig.COLOR_ACCENT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}

package com.smartcafe.view.auth;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Login screen.
 *
 * Layout: split-panel
 *   LEFT  (40 %) — branded coffee-brown panel with logo text and tagline
 *   RIGHT (60 %) — form: identifier field, password field, remember-me,
 *                  login button, navigation links
 *
 * FlatClientProperties are used for placeholder text and password reveal
 * so we don't have to maintain custom text-field subclasses.
 */
public class LoginPanel extends JPanel {

    private final AuthController controller;

    private JTextField     identifierField;
    private JPasswordField passwordField;
    private JCheckBox      rememberMeBox;

    public LoginPanel(AuthController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(AppConfig.COLOR_BG);
        buildUI();
    }

    private void buildUI() {
        add(buildLeftPanel(),  BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    // ── LEFT — branding ───────────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Vertical gradient: dark roast → espresso
                GradientPaint grad = new GradientPaint(
                        0, 0,            AppConfig.COLOR_PRIMARY_DARK,
                        0, getHeight(),  new Color(0x1A0E08));
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles (coffee rings effect)
                g2.setColor(new Color(255, 255, 255, 8));
                g2.setStroke(new BasicStroke(1.5f));
                int cx = getWidth() / 2, cy = getHeight() / 2 + 80;
                for (int r = 60; r <= 240; r += 60) {
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                }
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(480, AppConfig.WINDOW_HEIGHT));
        panel.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(0, 50, 0, 50));

        // Coffee cup icon
        JLabel cupLabel = new JLabel("☕");
        cupLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        cupLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // App name
        JLabel nameLabel = new JLabel("<html><span style='font-size:22pt;'>Smart Cafe</span><br/>"
                + "<span style='font-size:14pt;'>Management System</span></html>");
        nameLabel.setFont(AppConfig.FONT_HEADING);
        nameLabel.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Tagline
        JLabel tagline = new JLabel("<html><i>\"Where great coffee meets<br/>smart service.\"</i></html>");
        tagline.setFont(AppConfig.FONT_SUBTITLE);
        tagline.setForeground(AppConfig.COLOR_ACCENT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Version
        JLabel version = new JLabel("v" + AppConfig.APP_VERSION);
        version.setFont(AppConfig.FONT_SMALL);
        version.setForeground(new Color(255, 255, 255, 60));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(cupLabel);
        inner.add(Box.createVerticalStrut(16));
        inner.add(nameLabel);
        inner.add(Box.createVerticalStrut(24));
        inner.add(sep);
        inner.add(Box.createVerticalStrut(24));
        inner.add(tagline);
        inner.add(Box.createVerticalGlue());
        inner.add(version);

        panel.add(inner);
        return panel;
    }

    // ── RIGHT — form ──────────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AppConfig.COLOR_BG);

        JPanel form = buildForm();
        outer.add(form);
        return outer;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 20, 0, 20));
        form.setMaximumSize(new Dimension(400, 700));
        form.setPreferredSize(new Dimension(400, 600));

        // ── Title
        JLabel title = new JLabel("Welcome back");
        title.setFont(AppConfig.FONT_HEADING);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(AppConfig.FONT_SUBTITLE);
        subtitle.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Username / Email field
        JLabel idLabel = styledLabel("Username or Email");
        identifierField = new JTextField();
        styleField(identifierField, "Enter your username or email");
        identifierField.addKeyListener(loginOnEnter());

        // ── Password field
        JLabel pwLabel = styledLabel("Password");
        passwordField = new JPasswordField();
        styleField(passwordField, "Enter your password");
        passwordField.putClientProperty("JPasswordField.showRevealButton", true);
        passwordField.addKeyListener(loginOnEnter());

        // ── Remember me + Forgot password row
        rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setOpaque(false);
        rememberMeBox.setFont(AppConfig.FONT_BODY);
        rememberMeBox.setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        JButton forgotBtn = linkButton("Forgot password?");
        forgotBtn.addActionListener(e -> controller.showForgotPassword());

        JPanel rememberRow = new JPanel(new BorderLayout());
        rememberRow.setOpaque(false);
        rememberRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        rememberRow.add(rememberMeBox, BorderLayout.WEST);
        rememberRow.add(forgotBtn,     BorderLayout.EAST);
        rememberRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Login button
        RoundedButton loginBtn = new RoundedButton("SIGN IN", RoundedButton.Style.PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT + 4));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        // ── Divider with "or"
        JPanel divider = buildDivider("OR");

        // ── Sign-up link
        JPanel signupRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signupRow.setOpaque(false);
        signupRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        signupRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAccount = new JLabel("Don't have an account?");
        noAccount.setFont(AppConfig.FONT_BODY);
        noAccount.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JButton signupBtn = linkButton("Sign Up");
        signupBtn.addActionListener(e -> controller.showSignup());
        signupRow.add(noAccount);
        signupRow.add(signupBtn);

        // ── Assemble
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(36));
        form.add(idLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(identifierField);
        form.add(Box.createVerticalStrut(18));
        form.add(pwLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(12));
        form.add(rememberRow);
        form.add(Box.createVerticalStrut(24));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(24));
        form.add(divider);
        form.add(Box.createVerticalStrut(18));
        form.add(signupRow);

        return form;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doLogin() {
        controller.login(
                identifierField.getText().trim(),
                new String(passwordField.getPassword())
        );
    }

    private KeyAdapter loginOnEnter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void styleField(JTextField field, String placeholder) {
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 300);
        field.setFont(AppConfig.FONT_BODY);
        field.setPreferredSize(new Dimension(360, AppConfig.FIELD_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private static JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConfig.FONT_LABEL);
        lbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private static JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppConfig.FONT_BODY.deriveFont(Font.BOLD));
        btn.setForeground(AppConfig.COLOR_ACCENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JPanel buildDivider(String label) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.weightx = 1; c.gridx = 0;
        JSeparator left = new JSeparator();
        left.setForeground(AppConfig.COLOR_BORDER);
        p.add(left, c);

        c.weightx = 0; c.gridx = 1;
        JLabel lbl = new JLabel("  " + label + "  ");
        lbl.setFont(AppConfig.FONT_SMALL);
        lbl.setForeground(AppConfig.COLOR_TEXT_HINT);
        p.add(lbl, c);

        c.weightx = 1; c.gridx = 2;
        JSeparator right = new JSeparator();
        right.setForeground(AppConfig.COLOR_BORDER);
        p.add(right, c);

        return p;
    }

    /** Clears password field — called by MainFrame when navigating back to this panel. */
    public void resetForm() {
        passwordField.setText("");
        identifierField.requestFocusInWindow();
    }
}

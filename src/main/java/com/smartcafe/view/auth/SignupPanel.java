package com.smartcafe.view.auth;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.model.Role;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Signup (account creation) screen.
 *
 * When the database has no users yet (first-time setup) the Role combo is
 * hidden and a notice tells the operator that their account will be Admin.
 * Subsequent signups show all four roles.
 */
public class SignupPanel extends JPanel {

    private final AuthController controller;

    private JTextField     fullNameField;
    private JTextField     usernameField;
    private JTextField     emailField;
    private JTextField     phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JComboBox<Role> roleCombo;
    private JLabel          roleNotice;

    public SignupPanel(AuthController controller) {
        this.controller = controller;
        setBackground(AppConfig.COLOR_BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = buildForm();
        // Wrap in a scroll pane so small screens can still reach every field
        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(480, AppConfig.WINDOW_HEIGHT - 40));

        add(scroll);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(40, 60, 40, 60));

        // ── Header
        JLabel title = new JLabel("Create Account");
        title.setFont(AppConfig.FONT_HEADING);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Fill in the details below to register a new staff account.");
        sub.setFont(AppConfig.FONT_SUBTITLE);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Fields
        fullNameField = field("Full name", "e.g. Maria Santos");
        usernameField = field("Username", "3-50 chars, letters/digits/_");
        emailField    = field("Email address", "staff@cafe.com");
        phoneField    = field("Phone (optional)", "+63 912 345 6789");
        passwordField = pwField("Password (min. 8 characters)");
        confirmField  = pwField("Confirm password");

        // ── Role selector
        JLabel roleLabel = styledLabel("Role");
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleCombo = new JComboBox<>(Role.values());
        roleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Role r) setText(r.getDisplayName());
                return this;
            }
        });
        roleCombo.setSelectedItem(Role.CASHIER);
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT));
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleCombo.setFont(AppConfig.FONT_BODY);

        roleNotice = new JLabel("⭐ You are creating the first account — it will be Admin.");
        roleNotice.setFont(AppConfig.FONT_SMALL);
        roleNotice.setForeground(AppConfig.COLOR_ACCENT);
        roleNotice.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleNotice.setVisible(false);

        // ── Buttons
        RoundedButton signupBtn = new RoundedButton("CREATE ACCOUNT", RoundedButton.Style.PRIMARY);
        signupBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT + 4));
        signupBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signupBtn.addActionListener(e -> doSignup());

        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        loginRow.setOpaque(false);
        loginRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel already = new JLabel("Already have an account?  ");
        already.setFont(AppConfig.FONT_BODY);
        already.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JButton loginLink = linkButton("Sign In");
        loginLink.addActionListener(e -> controller.showLogin());
        loginRow.add(already);
        loginRow.add(loginLink);

        // ── Assemble
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(28));

        addRow(form, "Full Name",       fullNameField);
        addRow(form, "Username",        usernameField);
        addRow(form, "Email Address",   emailField);
        addRow(form, "Phone (optional)",phoneField);
        addRow(form, "Password",        passwordField);
        addRow(form, "Confirm Password",confirmField);

        form.add(roleLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(roleCombo);
        form.add(Box.createVerticalStrut(4));
        form.add(roleNotice);
        form.add(Box.createVerticalStrut(28));
        form.add(signupBtn);
        form.add(Box.createVerticalStrut(16));
        form.add(loginRow);

        return form;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doSignup() {
        Role selectedRole = (Role) roleCombo.getSelectedItem();
        controller.signup(
                fullNameField.getText().trim(),
                usernameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                new String(passwordField.getPassword()),
                new String(confirmField.getPassword()),
                selectedRole != null ? selectedRole : Role.CASHIER
        );
    }

    /** Called by MainFrame just before showing this panel. */
    public void refresh() {
        boolean isFirst = controller.isFirstTimeSetup();
        roleCombo.setVisible(!isFirst);
        roleNotice.setVisible(isFirst);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void addRow(JPanel parent, String label, JComponent field) {
        JLabel lbl = styledLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(6));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(field);
        parent.add(Box.createVerticalStrut(16));
    }

    private static JTextField field(String name, String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT));
        return f;
    }

    private static JPasswordField pwField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.putClientProperty("JPasswordField.showRevealButton", true);
        f.setFont(AppConfig.FONT_BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.FIELD_HEIGHT));
        return f;
    }

    private static JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConfig.FONT_LABEL);
        lbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
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
}

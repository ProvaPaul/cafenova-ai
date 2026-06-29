package com.smartcafe.view.dashboard;

import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.model.User;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.components.CardPanel;
import com.smartcafe.view.components.RoundedButton;
import com.smartcafe.view.components.SidebarButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin dashboard — full access to all modules.
 *
 * Structure:
 *   NORTH  → header bar (logo, title, user chip, logout)
 *   WEST   → sidebar navigation
 *   CENTER → scrollable content area
 *
 * Business module buttons show "Coming Soon" toasts; they will be wired
 * to real panels in subsequent implementation steps.
 */
public class AdminDashboard extends JPanel {

    private static final String[][] NAV = {
        {"🏠", "Dashboard"},
        {"👥", "User Management"},
        {"🍽️", "Menu"},
        {"🪑", "Tables"},
        {"📋", "Orders"},
        {"📦", "Inventory"},
        {"📊", "Reports"},
        {"🤖", "AI Insights"},
        {"⚙️", "Settings"},
    };

    private final AuthController controller;

    public AdminDashboard(AuthController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(AppConfig.COLOR_BG);
        build();
    }

    private void build() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AppConfig.COLOR_HEADER_BG);
        h.setPreferredSize(new Dimension(0, AppConfig.HEADER_HEIGHT));
        h.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Left: logo + app name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel cup  = new JLabel("☕");
        cup.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel name = new JLabel(AppConfig.APP_NAME);
        name.setFont(AppConfig.FONT_LABEL);
        name.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        left.add(cup);
        left.add(name);

        // Right: user info + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        User u = SessionManager.getCurrentUser();
        String displayName = u != null ? u.getFullName() : "—";
        String roleName    = u != null ? u.getRole().getDisplayName() : "";

        JLabel userLbl = new JLabel("<html><b>" + displayName + "</b>"
                + " &nbsp;<span style='color:#8B5E3C;'>" + roleName + "</span></html>");
        userLbl.setFont(AppConfig.FONT_BODY);
        userLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        RoundedButton logoutBtn = new RoundedButton("Sign Out", RoundedButton.Style.GHOST);
        logoutBtn.addActionListener(e -> controller.logout());

        right.add(userLbl);
        right.add(logoutBtn);

        h.add(left,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);

        // Bottom border
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.COLOR_BORDER),
                new EmptyBorder(0, 20, 0, 20)));
        return h;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(AppConfig.COLOR_SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(AppConfig.SIDEBAR_WIDTH, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, AppConfig.COLOR_BORDER),
                new EmptyBorder(16, 0, 16, 0)));

        SidebarButton[] btns = new SidebarButton[NAV.length];
        for (int i = 0; i < NAV.length; i++) {
            SidebarButton btn = new SidebarButton(NAV[i][0], NAV[i][1]);
            btns[i] = btn;
            int idx = i;
            btn.addActionListener(e -> {
                for (SidebarButton b : btns) b.setActive(false);
                btns[idx].setActive(true);
                if (idx > 0) showComingSoon(NAV[idx][1]);
            });
            sb.add(btn);
        }
        btns[0].setActive(true);    // Dashboard is selected by default

        sb.add(Box.createVerticalGlue());
        return sb;
    }

    // ── Content area ──────────────────────────────────────────────────────────

    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setBackground(AppConfig.COLOR_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Welcome
        User u = SessionManager.getCurrentUser();
        JLabel welcome = new JLabel("Good day, " + (u != null ? u.getFullName() : "Admin") + " 👋");
        welcome.setFont(AppConfig.FONT_TITLE);
        welcome.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Here's your cafe overview for today.");
        sub.setFont(AppConfig.FONT_SUBTITLE);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Stat cards
        JPanel statsRow = buildStatsRow(new String[][]{
            {"📋", "Today's Orders", "—"},
            {"💰", "Revenue",        "—"},
            {"⏳", "Pending",        "—"},
            {"🪑", "Tables Free",   "—"},
        });
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Coming Soon banner
        JPanel banner = buildBanner();
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(welcome);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(28));
        content.add(statsRow);
        content.add(Box.createVerticalStrut(24));
        content.add(banner);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(AppConfig.COLOR_BG);
        scroll.getViewport().setBackground(AppConfig.COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static JPanel buildStatsRow(String[][] stats) {
        JPanel row = new JPanel(new GridLayout(1, stats.length, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        for (String[] s : stats) {
            row.add(buildStatCard(s[0], s[1], s[2]));
        }
        return row;
    }

    private static CardPanel buildStatCard(String icon, String label, String value) {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(AppConfig.FONT_HEADING.deriveFont(26f));
        valueLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(AppConfig.FONT_SMALL);
        labelLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(iconLbl, BorderLayout.WEST);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(valueLbl);
        bottom.add(labelLbl);

        card.add(top,    BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    private static JPanel buildBanner() {
        CardPanel banner = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        banner.setLayout(new BorderLayout(0, 8));
        banner.setBorder(new EmptyBorder(24, 24, 24, 24));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel title = new JLabel("🚀  Business modules coming in the next step");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        JLabel sub = new JLabel(
            "Menu management, POS, kitchen queue, inventory, and AI insights will be added soon.");
        sub.setFont(AppConfig.FONT_SUBTITLE);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        banner.add(title, BorderLayout.NORTH);
        banner.add(sub,   BorderLayout.CENTER);
        return banner;
    }

    private void showComingSoon(String module) {
        JOptionPane.showMessageDialog(this,
                "<html><b>" + module + "</b> will be available in the next implementation step.</html>",
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
}

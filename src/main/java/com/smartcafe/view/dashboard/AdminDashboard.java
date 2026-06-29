package com.smartcafe.view.dashboard;

import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.model.User;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.components.RoundedButton;
import com.smartcafe.view.components.SidebarButton;
import com.smartcafe.view.panel.CategoryPanel;
import com.smartcafe.view.panel.DashboardHomePanel;
import com.smartcafe.view.panel.ProductPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin dashboard — full access to all implemented modules.
 *
 * Content area uses CardLayout; each sidebar button calls switchTo(key).
 * Panels that are not yet implemented show a "Coming Soon" placeholder.
 */
public class AdminDashboard extends JPanel {

    private static final String NAV_HOME       = "HOME";
    private static final String NAV_CATEGORIES = "CATEGORIES";
    private static final String NAV_PRODUCTS   = "PRODUCTS";

    private static final Object[][] NAV = {
        {"🏠",  "Dashboard",       NAV_HOME},
        {"🏷",  "Categories",      NAV_CATEGORIES},
        {"📦",  "Products",        NAV_PRODUCTS},
        {"👥",  "User Management", null},
        {"📋",  "Orders",          null},
        {"🏭",  "Inventory",       null},
        {"📊",  "Reports",         null},
        {"🤖",  "AI Insights",     null},
        {"⚙️",  "Settings",        null},
    };

    private final AuthController    controller;
    private final CardLayout        cardLayout  = new CardLayout();
    private final JPanel            contentArea = new JPanel(cardLayout);
    private       DashboardHomePanel homePanel;
    private       CategoryPanel      categoryPanel;
    private       ProductPanel       productPanel;
    private       SidebarButton[]    sidebarBtns;

    public AdminDashboard(AuthController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(AppConfig.COLOR_BG);

        buildContentArea();
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(contentArea,    BorderLayout.CENTER);
    }

    // ── Content area ──────────────────────────────────────────────────────────

    private void buildContentArea() {
        contentArea.setBackground(AppConfig.COLOR_BG);

        homePanel     = new DashboardHomePanel(
                () -> switchTo(NAV_CATEGORIES),
                () -> switchTo(NAV_PRODUCTS));
        categoryPanel = new CategoryPanel();
        productPanel  = new ProductPanel(false);

        contentArea.add(homePanel,     NAV_HOME);
        contentArea.add(categoryPanel, NAV_CATEGORIES);
        contentArea.add(productPanel,  NAV_PRODUCTS);
        contentArea.add(comingSoon("User Management"), "USER_MANAGEMENT");
        contentArea.add(comingSoon("Orders"),          "ORDERS");
        contentArea.add(comingSoon("Inventory"),       "INVENTORY");
        contentArea.add(comingSoon("Reports"),         "REPORTS");
        contentArea.add(comingSoon("AI Insights"),     "AI");
        contentArea.add(comingSoon("Settings"),        "SETTINGS");

        cardLayout.show(contentArea, NAV_HOME);
    }

    private void switchTo(String key) {
        cardLayout.show(contentArea, key);
        if (NAV_HOME.equals(key))       homePanel.refresh();
        if (NAV_CATEGORIES.equals(key)) categoryPanel.loadData();
        if (NAV_PRODUCTS.equals(key))   productPanel.loadData();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AppConfig.COLOR_HEADER_BG);
        h.setPreferredSize(new Dimension(0, AppConfig.HEADER_HEIGHT));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel cup = new JLabel("☕");
        cup.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel name = new JLabel(AppConfig.APP_NAME);
        name.setFont(AppConfig.FONT_LABEL);
        name.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        left.add(cup);
        left.add(name);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        User u = SessionManager.getCurrentUser();
        JLabel userLbl = new JLabel("<html><b>" + (u != null ? u.getFullName() : "—") + "</b>"
                + "&nbsp;<span style='color:#8B5E3C;'>" + (u != null ? u.getRole().getDisplayName() : "") + "</span></html>");
        userLbl.setFont(AppConfig.FONT_BODY);
        userLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        RoundedButton logoutBtn = new RoundedButton("Sign Out", RoundedButton.Style.GHOST);
        logoutBtn.addActionListener(e -> controller.logout());
        right.add(userLbl);
        right.add(logoutBtn);

        h.add(left,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
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

        sidebarBtns = new SidebarButton[NAV.length];
        for (int i = 0; i < NAV.length; i++) {
            String icon  = (String) NAV[i][0];
            String label = (String) NAV[i][1];
            String key   = (String) NAV[i][2];

            SidebarButton btn = new SidebarButton(icon, label);
            sidebarBtns[i] = btn;
            final int idx = i;
            btn.addActionListener(e -> {
                for (SidebarButton b : sidebarBtns) b.setActive(false);
                sidebarBtns[idx].setActive(true);
                if (key != null) switchTo(key);
            });
            sb.add(btn);
        }
        sidebarBtns[0].setActive(true);
        sb.add(Box.createVerticalGlue());
        return sb;
    }

    // ── Coming-soon placeholder ───────────────────────────────────────────────

    private static JPanel comingSoon(String moduleName) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppConfig.COLOR_BG);
        JLabel icon = new JLabel("🚀");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        JLabel title = new JLabel(moduleName + " — Coming Soon");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("This module will be implemented in the next step.");
        sub.setFont(AppConfig.FONT_SUBTITLE);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon);
        inner.add(Box.createVerticalStrut(12));
        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(sub);

        p.add(inner);
        return p;
    }

    /** Called by MainFrame each time this dashboard becomes visible after login. */
    public void refresh() {
        homePanel.refresh();
    }
}

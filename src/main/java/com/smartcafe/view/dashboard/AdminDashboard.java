package com.smartcafe.view.dashboard;

import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.model.User;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.components.RoundedButton;
import com.smartcafe.view.components.SidebarButton;
import com.smartcafe.view.panel.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboard extends JPanel {

    private static final String K_HOME      = "HOME";
    private static final String K_CATS      = "CATEGORIES";
    private static final String K_PRODUCTS  = "PRODUCTS";
    private static final String K_INVENTORY = "INVENTORY";
    private static final String K_POS       = "POS";
    private static final String K_BILLING   = "BILLING";

    private static final Object[][] NAV = {
        {"🏠",  "Dashboard",       K_HOME},
        {"🏷",  "Categories",      K_CATS},
        {"📦",  "Products",        K_PRODUCTS},
        {"👥",  "User Management", null},
        {"🖥️", "POS",              K_POS},
        {"🏭",  "Inventory",       K_INVENTORY},
        {"💳",  "Billing",         K_BILLING},
        {"📊",  "Reports",         null},
        {"🤖",  "AI Insights",     null},
        {"⚙️",  "Settings",        null},
    };

    private final AuthController    controller;
    private final CardLayout        cardLayout  = new CardLayout();
    private final JPanel            contentArea = new JPanel(cardLayout);

    private DashboardHomePanel homePanel;
    private CategoryPanel      categoryPanel;
    private ProductPanel       productPanel;
    private InventoryPanel     inventoryPanel;
    private POSPanel           posPanel;
    private BillHistoryPanel   billingPanel;
    private SidebarButton[]    sidebarBtns;

    public AdminDashboard(AuthController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(AppConfig.COLOR_BG);
        buildContentArea();
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(contentArea,    BorderLayout.CENTER);
    }

    private void buildContentArea() {
        contentArea.setBackground(AppConfig.COLOR_BG);

        homePanel      = new DashboardHomePanel(() -> switchTo(K_CATS), () -> switchTo(K_PRODUCTS));
        categoryPanel  = new CategoryPanel();
        productPanel   = new ProductPanel(false);
        inventoryPanel = new InventoryPanel();
        posPanel       = new POSPanel();
        billingPanel   = new BillHistoryPanel();

        contentArea.add(homePanel,      K_HOME);
        contentArea.add(categoryPanel,  K_CATS);
        contentArea.add(productPanel,   K_PRODUCTS);
        contentArea.add(inventoryPanel, K_INVENTORY);
        contentArea.add(posPanel,       K_POS);
        contentArea.add(billingPanel,   K_BILLING);
        contentArea.add(comingSoon("User Management"), "USER_MANAGEMENT");
        contentArea.add(comingSoon("Reports"),         "REPORTS");
        contentArea.add(comingSoon("AI Insights"),     "AI");
        contentArea.add(comingSoon("Settings"),        "SETTINGS");

        cardLayout.show(contentArea, K_HOME);
    }

    private void switchTo(String key) {
        cardLayout.show(contentArea, key);
        switch (key) {
            case K_HOME      -> homePanel.refresh();
            case K_CATS      -> categoryPanel.loadData();
            case K_PRODUCTS  -> productPanel.loadData();
            case K_INVENTORY -> inventoryPanel.loadData();
            case K_POS       -> posPanel.loadData();
            case K_BILLING   -> billingPanel.loadData();
        }
    }

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
        left.add(cup); left.add(name);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        User u = SessionManager.getCurrentUser();
        JLabel userLbl = new JLabel("<html><b>" + (u != null ? u.getFullName() : "—") + "</b>"
                + "&nbsp;<span style='color:#8B5E3C;'>"
                + (u != null ? u.getRole().getDisplayName() : "") + "</span></html>");
        userLbl.setFont(AppConfig.FONT_BODY);
        userLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        RoundedButton logoutBtn = new RoundedButton("Sign Out", RoundedButton.Style.GHOST);
        logoutBtn.addActionListener(e -> controller.logout());
        right.add(userLbl); right.add(logoutBtn);

        h.add(left,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.COLOR_BORDER),
                new EmptyBorder(0, 20, 0, 20)));
        return h;
    }

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

    private static JPanel comingSoon(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppConfig.COLOR_BG);
        JPanel inner = new JPanel(); inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        JLabel icon  = new JLabel("🚀"); icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        JLabel title = new JLabel(name + " — Coming Soon"); title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("This module will be implemented in a future step.");
        sub.setFont(AppConfig.FONT_SUBTITLE); sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        for (JLabel l : new JLabel[]{icon, title, sub}) l.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon); inner.add(Box.createVerticalStrut(12));
        inner.add(title); inner.add(Box.createVerticalStrut(6)); inner.add(sub);
        p.add(inner);
        return p;
    }

    public void refresh() { homePanel.refresh(); }
}

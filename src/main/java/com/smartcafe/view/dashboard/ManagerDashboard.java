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

public class ManagerDashboard extends JPanel {

    private static final String K_HOME      = "HOME";
    private static final String K_CATS      = "CATEGORIES";
    private static final String K_PRODUCTS  = "PRODUCTS";
    private static final String K_INVENTORY = "INVENTORY";
    private static final String K_POS       = "POS";
    private static final String K_BILLING   = "BILLING";
    private static final String K_CUSTOMERS = "CUSTOMERS";
    private static final String K_RESERVE   = "RESERVATIONS";
    private static final String K_REPORTS   = "REPORTS";
    private static final String K_SETTINGS  = "SETTINGS";

    private static final Object[][] NAV = {
        {"🏠",  "Dashboard",    K_HOME},
        {"🏷",  "Categories",   K_CATS},
        {"📦",  "Products",     K_PRODUCTS},
        {"🖥️", "POS",           K_POS},
        {"🏭",  "Inventory",    K_INVENTORY},
        {"💳",  "Billing",      K_BILLING},
        {"👥",  "Customers",    K_CUSTOMERS},
        {"📅",  "Reservations", K_RESERVE},
        {"📊",  "Reports",      K_REPORTS},
        {"⚙️",  "Settings",     K_SETTINGS},
    };

    private final AuthController controller;
    private final CardLayout     cardLayout  = new CardLayout();
    private final JPanel         contentArea = new JPanel(cardLayout);

    private DashboardHomePanel homePanel;
    private CategoryPanel      categoryPanel;
    private ProductPanel       productPanel;
    private InventoryPanel     inventoryPanel;
    private POSPanel           posPanel;
    private BillHistoryPanel   billingPanel;
    private CustomerPanel      customerPanel;
    private ReservationPanel   reservationPanel;
    private ReportPanel        reportPanel;
    private SettingsPanel      settingsPanel;
    private SidebarButton[]    sidebarBtns;

    public ManagerDashboard(AuthController controller) {
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

        homePanel        = new DashboardHomePanel(() -> switchTo(K_CATS), () -> switchTo(K_PRODUCTS));
        categoryPanel    = new CategoryPanel();
        productPanel     = new ProductPanel(false);
        inventoryPanel   = new InventoryPanel();
        posPanel         = new POSPanel();
        billingPanel     = new BillHistoryPanel();
        customerPanel    = new CustomerPanel();
        reservationPanel = new ReservationPanel();
        reportPanel      = new ReportPanel();
        settingsPanel    = new SettingsPanel();

        contentArea.add(homePanel,        K_HOME);
        contentArea.add(categoryPanel,    K_CATS);
        contentArea.add(productPanel,     K_PRODUCTS);
        contentArea.add(inventoryPanel,   K_INVENTORY);
        contentArea.add(posPanel,         K_POS);
        contentArea.add(billingPanel,     K_BILLING);
        contentArea.add(customerPanel,    K_CUSTOMERS);
        contentArea.add(reservationPanel, K_RESERVE);
        contentArea.add(reportPanel,      K_REPORTS);
        contentArea.add(settingsPanel,    K_SETTINGS);

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
            case K_CUSTOMERS -> customerPanel.loadData();
            case K_RESERVE   -> reservationPanel.loadData();
            case K_REPORTS   -> reportPanel.loadData();
            case K_SETTINGS  -> settingsPanel.loadData();
        }
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AppConfig.COLOR_HEADER_BG);
        h.setPreferredSize(new Dimension(0, AppConfig.HEADER_HEIGHT));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); left.setOpaque(false);
        JLabel cup = new JLabel("☕"); cup.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel nm  = new JLabel(AppConfig.APP_NAME); nm.setFont(AppConfig.FONT_LABEL);
        nm.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        left.add(cup); left.add(nm);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0)); right.setOpaque(false);
        User u = SessionManager.getCurrentUser();
        JLabel info = new JLabel("<html><b>" + (u != null ? u.getFullName() : "—") + "</b>"
                + "&nbsp;<span style='color:#8B5E3C;'>Manager</span></html>");
        info.setFont(AppConfig.FONT_BODY); info.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        RoundedButton logout = new RoundedButton("Sign Out", RoundedButton.Style.GHOST);
        logout.addActionListener(e -> controller.logout());
        right.add(info); right.add(logout);
        h.add(left, BorderLayout.WEST); h.add(right, BorderLayout.EAST);
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
            String icon = (String) NAV[i][0], label = (String) NAV[i][1], key = (String) NAV[i][2];
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

    public void refresh() { homePanel.refresh(); }
}

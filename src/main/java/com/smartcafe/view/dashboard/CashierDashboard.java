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

public class CashierDashboard extends JPanel {

    private static final String K_HOME     = "HOME";
    private static final String K_POS      = "POS";
    private static final String K_PRODUCTS = "PRODUCTS";
    private static final String K_BILLING  = "BILLING";
    private static final String K_CUSTOMERS= "CUSTOMERS";
    private static final String K_RESERVE  = "RESERVATIONS";

    private static final Object[][] NAV = {
        {"🏠",  "Dashboard",    K_HOME},
        {"🖥️", "POS",           K_POS},
        {"📦",  "Products",     K_PRODUCTS},
        {"💳",  "Billing",      K_BILLING},
        {"👥",  "Customers",    K_CUSTOMERS},
        {"📅",  "Reservations", K_RESERVE},
    };

    private final AuthController controller;
    private final CardLayout     cardLayout  = new CardLayout();
    private final JPanel         contentArea = new JPanel(cardLayout);

    private DashboardHomePanel homePanel;
    private POSPanel           posPanel;
    private ProductPanel       productPanel;
    private BillHistoryPanel   billingPanel;
    private CustomerPanel      customerPanel;
    private ReservationPanel   reservationPanel;
    private SidebarButton[]    sidebarBtns;

    public CashierDashboard(AuthController controller) {
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

        homePanel        = new DashboardHomePanel(() -> switchTo(K_POS), () -> switchTo(K_POS));
        posPanel         = new POSPanel();
        productPanel     = new ProductPanel(true);
        billingPanel     = new BillHistoryPanel();
        customerPanel    = new CustomerPanel();
        reservationPanel = new ReservationPanel();

        contentArea.add(homePanel,        K_HOME);
        contentArea.add(posPanel,         K_POS);
        contentArea.add(productPanel,     K_PRODUCTS);
        contentArea.add(billingPanel,     K_BILLING);
        contentArea.add(customerPanel,    K_CUSTOMERS);
        contentArea.add(reservationPanel, K_RESERVE);

        cardLayout.show(contentArea, K_HOME);
    }

    private void switchTo(String key) {
        cardLayout.show(contentArea, key);
        switch (key) {
            case K_HOME     -> homePanel.refresh();
            case K_POS      -> posPanel.loadData();
            case K_PRODUCTS -> productPanel.loadData();
            case K_BILLING  -> billingPanel.loadData();
            case K_CUSTOMERS-> customerPanel.loadData();
            case K_RESERVE  -> reservationPanel.loadData();
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
                + "&nbsp;<span style='color:#8B5E3C;'>Cashier</span></html>");
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

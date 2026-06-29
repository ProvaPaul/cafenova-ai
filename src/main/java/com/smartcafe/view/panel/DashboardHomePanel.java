package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.model.DashboardStats;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.components.CardPanel;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dashboard home panel — stat cards, quick actions, welcome message.
 *
 * Stats are loaded off the EDT via SwingWorker to keep the UI responsive.
 * Call {@link #refresh()} each time this panel becomes visible.
 */
public class DashboardHomePanel extends JPanel {

    // Callbacks so Quick-Actions can navigate to other panels
    private final Runnable onManageCategories;
    private final Runnable onManageProducts;

    // Stat card value labels — updated when stats arrive
    private JLabel salesValue, revenueValue, ordersValue,
                   customersValue, lowStockValue, productsValue;

    public DashboardHomePanel(Runnable onManageCategories, Runnable onManageProducts) {
        this.onManageCategories = onManageCategories;
        this.onManageProducts   = onManageProducts;

        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.setBackground(AppConfig.COLOR_BG);
        scroll.getViewport().setBackground(AppConfig.COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    // ── Content ───────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel p = new JPanel();
        p.setBackground(AppConfig.COLOR_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Welcome header
        var user     = SessionManager.getCurrentUser();
        var name     = user != null ? user.getFullName() : "there";
        var today    = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        JLabel welcome = label("Good day, " + name + " 👋", AppConfig.FONT_TITLE, AppConfig.COLOR_TEXT_PRIMARY);
        JLabel dateLbl = label(today, AppConfig.FONT_SUBTITLE, AppConfig.COLOR_TEXT_SECONDARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Stat cards (2 rows × 3 columns)
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        salesValue     = valueLbl("—");
        revenueValue   = valueLbl("—");
        ordersValue    = valueLbl("—");
        customersValue = valueLbl("—");
        lowStockValue  = valueLbl("—");
        productsValue  = valueLbl("—");

        statsGrid.add(statCard("📋", "Today's Sales",    AppConfig.COLOR_INFO,    salesValue));
        statsGrid.add(statCard("💰", "Revenue",          AppConfig.COLOR_SUCCESS, revenueValue));
        statsGrid.add(statCard("🛒", "Orders",           AppConfig.COLOR_PRIMARY, ordersValue));
        statsGrid.add(statCard("👥", "Customers",        AppConfig.COLOR_ACCENT,  customersValue));
        statsGrid.add(statCard("⚠️",  "Low Stock",       AppConfig.COLOR_WARNING, lowStockValue));
        statsGrid.add(statCard("📦", "Total Products",   AppConfig.COLOR_INFO,    productsValue));

        // ── Quick Actions
        JPanel qa = buildQuickActions();
        qa.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(welcome);
        p.add(Box.createVerticalStrut(4));
        p.add(dateLbl);
        p.add(Box.createVerticalStrut(28));
        p.add(statsGrid);
        p.add(Box.createVerticalStrut(24));
        p.add(qa);

        return p;
    }

    // ── Quick Actions ─────────────────────────────────────────────────────────

    private JPanel buildQuickActions() {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel title = label("⚡  Quick Actions", AppConfig.FONT_TITLE, AppConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        btns.add(qaButton("📋  New Order",     null,                  RoundedButton.Style.GHOST));
        btns.add(qaButton("🏷  Categories",    onManageCategories,    RoundedButton.Style.SECONDARY));
        btns.add(qaButton("📦  Products",      onManageProducts,      RoundedButton.Style.SECONDARY));
        btns.add(qaButton("📊  Reports",       null,                  RoundedButton.Style.GHOST));

        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private RoundedButton qaButton(String text, Runnable action, RoundedButton.Style style) {
        RoundedButton btn = new RoundedButton(text, style, 10);
        btn.setPreferredSize(new Dimension(160, 38));
        if (action != null) {
            btn.addActionListener(e -> action.run());
        } else {
            btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    text.trim() + " will be available in the next step.",
                    "Coming Soon", JOptionPane.INFORMATION_MESSAGE));
            btn.setToolTipText("Coming soon");
        }
        return btn;
    }

    // ── Stat loading (SwingWorker) ────────────────────────────────────────────

    public void refresh() {
        // Set loading state immediately on EDT
        for (JLabel lbl : new JLabel[]{salesValue, revenueValue, ordersValue,
                                        customersValue, lowStockValue, productsValue}) {
            if (lbl != null) lbl.setText("…");
        }

        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() {
                return AppContext.dashboardService().getStats();
            }

            @Override
            protected void done() {
                try {
                    DashboardStats s = get();
                    NumberFormat currFmt = NumberFormat.getNumberInstance(Locale.US);
                    currFmt.setMinimumFractionDigits(2);
                    currFmt.setMaximumFractionDigits(2);

                    salesValue.setText(String.valueOf(s.getTodaySales()));
                    revenueValue.setText("₱ " + currFmt.format(s.getTodayRevenue()));
                    ordersValue.setText(String.valueOf(s.getTodayOrders()));
                    customersValue.setText(String.valueOf(s.getTablesUsedToday()));
                    lowStockValue.setText(String.valueOf(s.getLowStockProducts()));
                    productsValue.setText(String.valueOf(s.getTotalProducts()));
                } catch (Exception ignored) {
                    // DB offline or init race — silently leave "…"
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static CardPanel statCard(String icon, String cardLabel, Color accentColor, JLabel valueLabel) {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Coloured top accent bar
        JPanel topBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 6));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        valueLabel.setFont(AppConfig.FONT_HEADING.deriveFont(Font.BOLD, 28f));
        valueLabel.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        JLabel nameLbl = new JLabel(cardLabel);
        nameLbl.setFont(AppConfig.FONT_SMALL);
        nameLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(iconLbl, BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(valueLabel);
        body.add(Box.createVerticalStrut(2));
        body.add(nameLbl);

        card.add(topBar,    BorderLayout.NORTH);
        card.add(headerRow, BorderLayout.WEST);
        card.add(body,      BorderLayout.CENTER);
        return card;
    }

    private static JLabel valueLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_HEADING.deriveFont(Font.BOLD, 28f));
        l.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        return l;
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }
}

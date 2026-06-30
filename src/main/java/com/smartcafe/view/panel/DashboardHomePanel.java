package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.model.DashboardStats;
import com.smartcafe.model.InventoryItem;
import com.smartcafe.model.Order;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.components.CardPanel;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardHomePanel extends JPanel {

    private final Runnable onManageCategories;
    private final Runnable onManageProducts;
    private final Runnable onManageOrders;

    // Stat card labels
    private JLabel salesValue, revenueValue, ordersValue,
                   pendingValue, preparingValue, readyValue;

    // Recent orders table
    private final DefaultTableModel recentModel;
    private final JTable            recentTable;

    // Top products table
    private final DefaultTableModel topModel;
    private final JTable            topTable;

    // Low stock panel
    private final JPanel lowStockContent;

    public DashboardHomePanel(Runnable onManageCategories, Runnable onManageProducts) {
        this(onManageCategories, onManageProducts, null);
    }

    public DashboardHomePanel(Runnable onManageCategories, Runnable onManageProducts, Runnable onManageOrders) {
        this.onManageCategories = onManageCategories;
        this.onManageProducts   = onManageProducts;
        this.onManageOrders     = onManageOrders;

        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout());

        // Pre-build tables
        recentModel = new DefaultTableModel(new String[]{"Order #", "Customer", "Status", "Total", "Time"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = buildSmallTable(recentModel);
        recentTable.getColumnModel().getColumn(2).setCellRenderer(orderStatusRenderer());

        topModel = new DefaultTableModel(new String[]{"Product", "Qty Sold", "Revenue"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        topTable = buildSmallTable(topModel);
        DefaultTableCellRenderer rightAlignR = new DefaultTableCellRenderer();
        rightAlignR.setHorizontalAlignment(SwingConstants.RIGHT);
        topTable.getColumnModel().getColumn(1).setCellRenderer(rightAlignR);
        topTable.getColumnModel().getColumn(2).setCellRenderer(rightAlignR);

        lowStockContent = new JPanel();
        lowStockContent.setOpaque(false);
        lowStockContent.setLayout(new BoxLayout(lowStockContent, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.setBackground(AppConfig.COLOR_BG);
        scroll.getViewport().setBackground(AppConfig.COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildContent() {
        JPanel p = new JPanel();
        p.setBackground(AppConfig.COLOR_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        var user  = SessionManager.getCurrentUser();
        var name  = user != null ? user.getFullName() : "there";
        var today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        JLabel welcome = label("Good day, " + name + " 👋", AppConfig.FONT_TITLE, AppConfig.COLOR_TEXT_PRIMARY);
        JLabel dateLbl = label(today, AppConfig.FONT_SUBTITLE, AppConfig.COLOR_TEXT_SECONDARY);
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        dateLbl.setAlignmentX(LEFT_ALIGNMENT);

        // ── Row 1: today's totals ─────────────────────────────────────────────
        JPanel row1 = new JPanel(new GridLayout(1, 3, 14, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row1.setAlignmentX(LEFT_ALIGNMENT);

        salesValue   = valueLbl("—");
        revenueValue = valueLbl("—");
        ordersValue  = valueLbl("—");

        row1.add(statCard("📋", "Today's Sales",   AppConfig.COLOR_INFO,    salesValue));
        row1.add(statCard("💰", "Today's Revenue", AppConfig.COLOR_SUCCESS, revenueValue));
        row1.add(statCard("🛒", "Today's Orders",  AppConfig.COLOR_PRIMARY, ordersValue));

        // ── Row 2: active order counts ────────────────────────────────────────
        JPanel row2 = new JPanel(new GridLayout(1, 3, 14, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row2.setAlignmentX(LEFT_ALIGNMENT);

        pendingValue   = valueLbl("—");
        preparingValue = valueLbl("—");
        readyValue     = valueLbl("—");

        row2.add(statCard("⏳", "Pending",   new Color(0xFFC107), pendingValue));
        row2.add(statCard("👨‍🍳", "Preparing", new Color(0xFD7E14), preparingValue));
        row2.add(statCard("🔔", "Ready",     new Color(0x28A745), readyValue));

        // ── Bottom row: 3 columns ─────────────────────────────────────────────
        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 14, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        bottomRow.add(buildTableCard("🕐 Recent Orders",  new JScrollPane(recentTable)));
        bottomRow.add(buildTableCard("🏆 Top Products",   new JScrollPane(topTable)));
        bottomRow.add(buildLowStockCard());

        // ── Quick Actions ─────────────────────────────────────────────────────
        JPanel qa = buildQuickActions();
        qa.setAlignmentX(LEFT_ALIGNMENT);

        p.add(welcome);
        p.add(Box.createVerticalStrut(4));
        p.add(dateLbl);
        p.add(Box.createVerticalStrut(24));
        p.add(row1);
        p.add(Box.createVerticalStrut(14));
        p.add(row2);
        p.add(Box.createVerticalStrut(20));
        p.add(qa);
        p.add(Box.createVerticalStrut(20));
        p.add(bottomRow);

        return p;
    }

    private JPanel buildQuickActions() {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel title = label("⚡  Quick Actions", AppConfig.FONT_TITLE, AppConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.add(qaButton("📋  Orders",     onManageOrders,      RoundedButton.Style.PRIMARY));
        btns.add(qaButton("🏷  Categories", onManageCategories,  RoundedButton.Style.SECONDARY));
        btns.add(qaButton("📦  Products",   onManageProducts,    RoundedButton.Style.SECONDARY));
        btns.add(qaButton("📊  Reports",    null,                RoundedButton.Style.GHOST));
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private RoundedButton qaButton(String text, Runnable action, RoundedButton.Style style) {
        RoundedButton btn = new RoundedButton(text, style, 10);
        btn.setPreferredSize(new Dimension(150, 36));
        if (action != null) btn.addActionListener(e -> action.run());
        else {
            btn.setToolTipText("Coming soon");
            btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    text.trim() + " will navigate here.", "Navigate", JOptionPane.INFORMATION_MESSAGE));
        }
        return btn;
    }

    private static CardPanel buildTableCard(String heading, JScrollPane scroll) {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel hdr = new JLabel(heading);
        hdr.setFont(AppConfig.FONT_LABEL);
        hdr.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        scroll.setBorder(null);
        scroll.setBackground(AppConfig.COLOR_SURFACE);
        scroll.getViewport().setBackground(AppConfig.COLOR_SURFACE);
        card.add(hdr,    BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private CardPanel buildLowStockCard() {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel hdr = new JLabel("⚠️  Low Stock Alerts");
        hdr.setFont(AppConfig.FONT_LABEL);
        hdr.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JScrollPane scroll = new JScrollPane(lowStockContent);
        scroll.setBorder(null);
        scroll.setBackground(AppConfig.COLOR_SURFACE);
        scroll.getViewport().setBackground(AppConfig.COLOR_SURFACE);
        card.add(hdr,    BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    public void refresh() {
        for (JLabel l : new JLabel[]{salesValue, revenueValue, ordersValue,
                                      pendingValue, preparingValue, readyValue}) {
            if (l != null) l.setText("…");
        }

        new SwingWorker<DashboardStats, Void>() {
            @Override protected DashboardStats doInBackground() {
                return AppContext.dashboardService().getStats();
            }
            @Override protected void done() {
                try {
                    DashboardStats s = get();
                    NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                    fmt.setMinimumFractionDigits(2);
                    fmt.setMaximumFractionDigits(2);

                    salesValue.setText(String.valueOf(s.getTodaySales()));
                    revenueValue.setText("₱ " + fmt.format(s.getTodayRevenue()));
                    ordersValue.setText(String.valueOf(s.getTodayOrders()));
                    pendingValue.setText(String.valueOf(s.getPendingOrders()));
                    preparingValue.setText(String.valueOf(s.getPreparingOrders()));
                    readyValue.setText(String.valueOf(s.getReadyOrders()));

                    updateRecentOrders(s.getRecentOrders());
                    updateTopProducts(s.getTopProducts());
                    updateLowStock(s.getLowStockItems());
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void updateRecentOrders(List<Order> orders) {
        recentModel.setRowCount(0);
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        for (Order o : orders) {
            recentModel.addRow(new Object[]{
                o.getOrderNumber(),
                o.getCustomerName() != null ? o.getCustomerName() : "Walk-in",
                o.getStatus(),
                String.format("₱%.0f", o.getTotal()),
                o.getCreatedAt() != null ? o.getCreatedAt().format(tf) : "—"
            });
        }
    }

    private void updateTopProducts(List<Map<String, Object>> tops) {
        topModel.setRowCount(0);
        for (Map<String, Object> row : tops) {
            topModel.addRow(new Object[]{
                row.get("name"),
                row.get("qtySold"),
                String.format("₱%.0f", ((Number) row.get("revenue")).doubleValue())
            });
        }
    }

    private void updateLowStock(List<InventoryItem> items) {
        lowStockContent.removeAll();
        if (items.isEmpty()) {
            JLabel ok = new JLabel("✅ All stock levels OK");
            ok.setFont(AppConfig.FONT_BODY);
            ok.setForeground(AppConfig.COLOR_SUCCESS);
            ok.setBorder(new EmptyBorder(4, 4, 4, 4));
            lowStockContent.add(ok);
        } else {
            for (InventoryItem it : items) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(3, 4, 3, 4));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

                JLabel name = new JLabel("⚠ " + it.getName());
                name.setFont(AppConfig.FONT_SMALL);
                name.setForeground(AppConfig.COLOR_WARNING);

                JLabel qty = new JLabel(String.format("%.1f %s", it.getCurrentStock(), it.getUnit()));
                qty.setFont(AppConfig.FONT_SMALL);
                qty.setForeground(AppConfig.COLOR_TEXT_HINT);

                row.add(name, BorderLayout.WEST);
                row.add(qty,  BorderLayout.EAST);
                lowStockContent.add(row);
            }
        }
        lowStockContent.revalidate();
        lowStockContent.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static CardPanel statCard(String icon, String cardLabel, Color accentColor, JLabel valueLabel) {
        CardPanel card = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel topBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 5));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        valueLabel.setFont(AppConfig.FONT_HEADING.deriveFont(Font.BOLD, 26f));
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

    private static JTable buildSmallTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(AppConfig.FONT_SMALL);
        t.setRowHeight(26);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(AppConfig.COLOR_BORDER);
        t.setBackground(AppConfig.COLOR_SURFACE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(AppConfig.FONT_SMALL);
        t.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        t.getTableHeader().setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return t;
    }

    private static DefaultTableCellRenderer orderStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                String s = v != null ? v.toString() : "";
                setForeground(Order.statusColor(s));
                setText(Order.statusLabel(s));
                return this;
            }
        };
    }

    private static JLabel valueLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_HEADING.deriveFont(Font.BOLD, 26f));
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

package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Order;
import com.smartcafe.model.OrderItem;
import com.smartcafe.util.PermissionManager;
import com.smartcafe.view.components.CardPanel;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Full order lifecycle management panel.
 * Shows all active orders (NEW→PENDING→CONFIRMED→PREPARING→READY→SERVED→COMPLETED / CANCELLED)
 * with status-advance buttons, details view, and cancellation.
 */
public class OrderManagementPanel extends JPanel {

    private static final String[] COLS = {
        "ID", "Order #", "Type", "Customer / Table", "Status", "Total", "Time"};
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("MMM d HH:mm");

    private final DefaultTableModel model;
    private final JTable            table;
    private       List<Order>       orders = new ArrayList<>();

    private final JComboBox<String> statusFilter;
    private final JLabel            countLbl;

    // Action buttons
    private final RoundedButton advanceBtn, cancelBtn, detailsBtn, refreshBtn;

    // Right panel - order details
    private final JPanel detailsPanel;
    private final JLabel detailOrderNum, detailCustomer, detailStatus, detailType, detailTotal;
    private final DefaultTableModel itemsModel;
    private final JTable itemsTable;

    public OrderManagementPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Order Management");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Track and manage the full order lifecycle.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // ── Toolbar ────────────────────────────────────────────────────────────
        statusFilter = new JComboBox<>(new String[]{
            "ALL ACTIVE", "NEW", "PENDING", "CONFIRMED", "PREPARING", "READY", "SERVED",
            "COMPLETED", "CANCELLED"});
        statusFilter.setFont(AppConfig.FONT_BODY);
        statusFilter.setPreferredSize(new Dimension(160, 32));
        statusFilter.addActionListener(e -> applyFilter());

        refreshBtn  = new RoundedButton("↻ Refresh",       RoundedButton.Style.GHOST);
        advanceBtn  = new RoundedButton("▶ Advance Status", RoundedButton.Style.PRIMARY);
        cancelBtn   = new RoundedButton("✕ Cancel Order",  RoundedButton.Style.DANGER);
        detailsBtn  = new RoundedButton("📋 View Items",   RoundedButton.Style.SECONDARY);
        advanceBtn.setEnabled(false); cancelBtn.setEnabled(false); detailsBtn.setEnabled(false);

        refreshBtn.addActionListener(e -> loadData());
        advanceBtn.addActionListener(e -> advanceStatus());
        cancelBtn.addActionListener(e  -> cancelOrder());
        detailsBtn.addActionListener(e -> showDetails());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(AppConfig.FONT_SMALL);
        filterLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        toolbar.add(filterLbl); toolbar.add(statusFilter);
        toolbar.add(refreshBtn); toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(detailsBtn); toolbar.add(advanceBtn); toolbar.add(cancelBtn);

        countLbl = new JLabel(" ");
        countLbl.setFont(AppConfig.FONT_SMALL);
        countLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        // ── Order table ────────────────────────────────────────────────────────
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(AppConfig.FONT_BODY);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.COLOR_BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(AppConfig.FONT_LABEL);
        table.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        table.getTableHeader().setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        // Hide ID col
        hideCol(0);
        // Status column coloured
        table.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());
        // Right-align total
        DefaultTableCellRenderer ra = new DefaultTableCellRenderer();
        ra.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(ra);

        table.getSelectionModel().addListSelectionListener(e -> onSelectionChange());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showDetails();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
        tablePanel.setOpaque(false);
        tablePanel.add(toolbar, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(countLbl, BorderLayout.SOUTH);

        // ── Details side panel ────────────────────────────────────────────────
        detailsPanel = buildDetailsPanel();
        detailsPanel.setVisible(false);

        // Order #
        detailOrderNum = new JLabel("—");
        detailOrderNum.setFont(AppConfig.FONT_LABEL);
        detailOrderNum.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        detailCustomer = new JLabel("—");
        detailCustomer.setFont(AppConfig.FONT_BODY);
        detailCustomer.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        detailStatus = new JLabel("—");
        detailStatus.setFont(AppConfig.FONT_LABEL);
        detailType = new JLabel("—");
        detailType.setFont(AppConfig.FONT_BODY);
        detailType.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        detailTotal = new JLabel("—");
        detailTotal.setFont(AppConfig.FONT_LABEL);
        detailTotal.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        String[] itemCols = {"Item", "Qty", "Price", "Subtotal"};
        itemsModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        itemsTable.setFont(AppConfig.FONT_SMALL);
        itemsTable.setRowHeight(30);
        itemsTable.setShowVerticalLines(false);
        itemsTable.setGridColor(AppConfig.COLOR_BORDER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tablePanel, buildDetailsPanel());
        split.setDividerLocation(0.65);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        split.setBackground(AppConfig.COLOR_BG);

        add(split, BorderLayout.CENTER);
    }

    private JPanel buildDetailsPanel() {
        CardPanel panel = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setMinimumSize(new Dimension(280, 0));
        panel.setPreferredSize(new Dimension(320, 0));

        JLabel hdr = new JLabel("Order Details");
        hdr.setFont(AppConfig.FONT_TITLE);
        hdr.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 6, 10));
        infoGrid.setOpaque(false);

        infoGrid.add(smallGrayLabel("Order #"));
        JLabel onLbl = new JLabel("—");
        onLbl.setFont(AppConfig.FONT_LABEL);
        onLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        infoGrid.add(onLbl);

        infoGrid.add(smallGrayLabel("Customer"));
        JLabel custLbl = new JLabel("—");
        custLbl.setFont(AppConfig.FONT_BODY);
        custLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        infoGrid.add(custLbl);

        infoGrid.add(smallGrayLabel("Status"));
        JLabel stLbl = new JLabel("—");
        stLbl.setFont(AppConfig.FONT_LABEL);
        infoGrid.add(stLbl);

        infoGrid.add(smallGrayLabel("Type"));
        JLabel typLbl = new JLabel("—");
        typLbl.setFont(AppConfig.FONT_BODY);
        typLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        infoGrid.add(typLbl);

        infoGrid.add(smallGrayLabel("Total"));
        JLabel totLbl = new JLabel("—");
        totLbl.setFont(AppConfig.FONT_LABEL);
        totLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        infoGrid.add(totLbl);

        // Store references to dynamically update
        panel.putClientProperty("onLbl",   onLbl);
        panel.putClientProperty("custLbl", custLbl);
        panel.putClientProperty("stLbl",   stLbl);
        panel.putClientProperty("typLbl",  typLbl);
        panel.putClientProperty("totLbl",  totLbl);

        String[] itemCols = {"Item", "Qty", "Price"};
        DefaultTableModel im = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable it = new JTable(im);
        it.setFont(AppConfig.FONT_SMALL);
        it.setRowHeight(28);
        it.setShowVerticalLines(false);
        it.setGridColor(AppConfig.COLOR_BORDER);
        it.getTableHeader().setFont(AppConfig.FONT_SMALL);
        panel.putClientProperty("itemsModel", im);

        JLabel itemsHdr = new JLabel("Items");
        itemsHdr.setFont(AppConfig.FONT_LABEL);
        itemsHdr.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        JLabel placeholder = new JLabel("Select an order to see details",
                SwingConstants.CENTER);
        placeholder.setFont(AppConfig.FONT_BODY);
        placeholder.setForeground(AppConfig.COLOR_TEXT_HINT);
        panel.putClientProperty("placeholder", placeholder);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(infoGrid, BorderLayout.NORTH);
        body.add(itemsHdr, BorderLayout.CENTER);
        // items table added after
        JScrollPane itScroll = new JScrollPane(it);
        panel.putClientProperty("itScroll", itScroll);
        body.add(itScroll, BorderLayout.SOUTH);

        panel.add(hdr, BorderLayout.NORTH);
        panel.add(placeholder, BorderLayout.CENTER);
        panel.putClientProperty("body", body);

        return panel;
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public void loadData() {
        try {
            orders = AppContext.orderService().findAll();
        } catch (AppException ex) {
            showError(ex.getMessage());
            orders = new ArrayList<>();
        }
        applyFilter();
    }

    private void applyFilter() {
        String selected = (String) statusFilter.getSelectedItem();
        model.setRowCount(0);
        for (Order o : orders) {
            boolean show;
            if ("ALL ACTIVE".equals(selected)) {
                show = !Order.STATUS_COMPLETED.equals(o.getStatus())
                    && !Order.STATUS_CANCELLED.equals(o.getStatus());
            } else if ("ALL".equals(selected)) {
                show = true;
            } else {
                show = selected.equals(o.getStatus())
                    || (("PREPARING".equals(selected)) && Order.STATUS_IN_PROGRESS.equals(o.getStatus()));
            }
            if (show) {
                String loc = o.getTableNumber() != null
                        ? (o.getCustomerName() != null ? o.getCustomerName() + " · T" + o.getTableNumber()
                                                       : "Table " + o.getTableNumber())
                        : (o.getCustomerName() != null ? o.getCustomerName() : "Walk-in");
                model.addRow(new Object[]{
                    o.getId(),
                    o.getOrderNumber(),
                    o.getOrderType() != null ? o.getOrderType().replace("_", " ") : "—",
                    loc,
                    o.getStatus(),
                    String.format("₱ %.2f", o.getTotal()),
                    o.getCreatedAt() != null ? (isToday(o) ? o.getCreatedAt().format(TIME_FMT)
                                                           : o.getCreatedAt().format(DT_FMT)) : "—"
                });
            }
        }
        countLbl.setText(model.getRowCount() + " order(s)");
        advanceBtn.setEnabled(false); cancelBtn.setEnabled(false); detailsBtn.setEnabled(false);
    }

    private boolean isToday(Order o) {
        if (o.getCreatedAt() == null) return false;
        return o.getCreatedAt().toLocalDate().equals(java.time.LocalDate.now());
    }

    // ── Selection change ──────────────────────────────────────────────────────

    private void onSelectionChange() {
        Order o = getSelected();
        if (o == null) {
            advanceBtn.setEnabled(false); cancelBtn.setEnabled(false); detailsBtn.setEnabled(false);
            clearDetailsPanel();
            return;
        }
        detailsBtn.setEnabled(true);
        String[] nexts = PermissionManager.nextStatuses(o.getStatus());
        advanceBtn.setEnabled(nexts.length > 0 && PermissionManager.canSetOrderStatus(nexts.length > 0 ? nexts[0] : ""));
        if (nexts.length > 0) advanceBtn.setText("▶ → " + nexts[0]);
        else advanceBtn.setText("▶ Advance Status");
        cancelBtn.setEnabled(PermissionManager.canSetOrderStatus(Order.STATUS_CANCELLED)
                && !Order.STATUS_COMPLETED.equals(o.getStatus())
                && !Order.STATUS_CANCELLED.equals(o.getStatus()));
        updateDetailsPanel(o);
    }

    private void updateDetailsPanel(Order o) {
        // Find the details panel (right of the split)
        Component[] comps = getComponents();
        for (Component c : comps) {
            if (c instanceof JSplitPane sp) {
                CardPanel dp = (CardPanel) sp.getRightComponent();
                updateCardPanel(dp, o);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateCardPanel(CardPanel dp, Order o) {
        JLabel onLbl   = (JLabel) dp.getClientProperty("onLbl");
        JLabel custLbl = (JLabel) dp.getClientProperty("custLbl");
        JLabel stLbl   = (JLabel) dp.getClientProperty("stLbl");
        JLabel typLbl  = (JLabel) dp.getClientProperty("typLbl");
        JLabel totLbl  = (JLabel) dp.getClientProperty("totLbl");
        JLabel phLbl   = (JLabel) dp.getClientProperty("placeholder");
        JPanel body    = (JPanel) dp.getClientProperty("body");
        DefaultTableModel im = (DefaultTableModel) dp.getClientProperty("itemsModel");

        if (onLbl == null) return;

        onLbl.setText(o.getOrderNumber() != null ? o.getOrderNumber() : "—");
        custLbl.setText(o.getCustomerName() != null ? o.getCustomerName() : "Walk-in");
        stLbl.setText(Order.statusLabel(o.getStatus()));
        stLbl.setForeground(Order.statusColor(o.getStatus()));
        typLbl.setText(o.getOrderType() != null ? o.getOrderType().replace("_", " ") : "—");
        totLbl.setText(String.format("₱ %.2f", o.getTotal()));

        if (phLbl != null) phLbl.setVisible(false);
        if (body != null)  body.setVisible(true);

        // Load items in background
        if (im != null) {
            im.setRowCount(0);
            new SwingWorker<List<OrderItem>, Void>() {
                @Override protected List<OrderItem> doInBackground() {
                    try { return AppContext.orderService().findItemsByOrderId(o.getId()); }
                    catch (Exception ex) { return List.of(); }
                }
                @Override protected void done() {
                    try {
                        im.setRowCount(0);
                        for (OrderItem it : get()) {
                            im.addRow(new Object[]{
                                it.getProductName() != null ? it.getProductName() : "Item #" + it.getMenuItemId(),
                                it.getQuantity(),
                                String.format("₱ %.2f", it.getUnitPrice())
                            });
                        }
                    } catch (Exception ignored) {}
                }
            }.execute();
        }

        // Swap placeholder for body in the card
        dp.remove(phLbl); dp.remove(body);
        dp.add(body, BorderLayout.CENTER);
        dp.revalidate(); dp.repaint();
    }

    private void clearDetailsPanel() {
        for (Component c : getComponents()) {
            if (c instanceof JSplitPane sp) {
                CardPanel dp = (CardPanel) sp.getRightComponent();
                JLabel phLbl = (JLabel) dp.getClientProperty("placeholder");
                JPanel body  = (JPanel) dp.getClientProperty("body");
                if (phLbl != null && body != null) {
                    dp.remove(body);
                    dp.add(phLbl, BorderLayout.CENTER);
                    dp.revalidate(); dp.repaint();
                }
                break;
            }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void advanceStatus() {
        Order o = getSelected();
        if (o == null) return;
        String[] nexts = PermissionManager.nextStatuses(o.getStatus());
        if (nexts.length == 0) { showInfo("Order is already in a terminal state."); return; }

        String target = nexts[0];
        if (!PermissionManager.canSetOrderStatus(target)) {
            showInfo("You do not have permission to set status: " + target); return;
        }

        int ch = JOptionPane.showConfirmDialog(this,
                "Change order " + o.getOrderNumber() + " status to: " + target + "?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);
        if (ch != JOptionPane.YES_OPTION) return;

        try {
            AppContext.orderService().updateStatus(o.getId(), target);
            loadData();
        } catch (AppException ex) { showError(ex.getMessage()); }
    }

    private void cancelOrder() {
        Order o = getSelected();
        if (o == null) return;
        String reason = JOptionPane.showInputDialog(this,
                "Reason for cancellation (optional):", "Cancel Order", JOptionPane.WARNING_MESSAGE);
        if (reason == null) return; // user pressed Cancel on dialog
        try {
            AppContext.orderService().updateStatus(o.getId(), Order.STATUS_CANCELLED);
            loadData();
        } catch (AppException ex) { showError(ex.getMessage()); }
    }

    private void showDetails() {
        Order o = getSelected();
        if (o == null) return;
        try {
            Order full = AppContext.billingService().findOrderDetails(o.getId());
            new InvoiceDialog(SwingUtilities.getWindowAncestor(this), full).setVisible(true);
        } catch (AppException ex) { showError(ex.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Order getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return orders.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    private void hideCol(int col) {
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(col).setMaxWidth(0);
        cm.getColumn(col).setMinWidth(0);
        cm.getColumn(col).setPreferredWidth(0);
    }

    private static DefaultTableCellRenderer statusRenderer() {
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

    private static JLabel smallGrayLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_SMALL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}

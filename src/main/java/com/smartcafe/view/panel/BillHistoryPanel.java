package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Order;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Billing / Bill History panel.
 * Shows a filterable, date-ranged list of orders with view-receipt and void-order actions.
 */
public class BillHistoryPanel extends JPanel {

    private static final String[] COLS =
        {"ID", "Order #", "Type", "Customer", "Table", "Cashier", "Status", "Total", "Date"};
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final DefaultTableModel model;
    private final JTable            table;
    private       List<Order>       orders = new ArrayList<>();

    private JSpinner  fromSpinner, toSpinner;
    private JComboBox<String> statusCombo;
    private final RoundedButton viewBtn, voidBtn;

    public BillHistoryPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("Billing & History");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("View, reprint, and void past orders.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        // ── Filter toolbar ────────────────────────────────────────────────────
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();
        fromSpinner = new JSpinner(fromModel);
        toSpinner   = new JSpinner(toModel);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));
        fromSpinner.setPreferredSize(new Dimension(130, 32));
        toSpinner.setPreferredSize(new Dimension(130, 32));

        // Default: last 30 days
        java.util.Calendar cal = java.util.Calendar.getInstance();
        toModel.setValue(cal.getTime());
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        fromModel.setValue(cal.getTime());

        statusCombo = new JComboBox<>(new String[]{
            "ALL", Order.STATUS_COMPLETED, Order.STATUS_PENDING,
            Order.STATUS_IN_PROGRESS, Order.STATUS_CANCELLED});
        statusCombo.setFont(AppConfig.FONT_BODY);
        statusCombo.setPreferredSize(new Dimension(140, 32));

        RoundedButton searchBtn = new RoundedButton("🔍  Search", RoundedButton.Style.PRIMARY);
        searchBtn.addActionListener(e -> loadData());

        viewBtn = new RoundedButton("View Receipt", RoundedButton.Style.SECONDARY);
        voidBtn = new RoundedButton("Void Order",   RoundedButton.Style.DANGER);
        viewBtn.setEnabled(false); voidBtn.setEnabled(false);
        viewBtn.addActionListener(e -> viewReceipt());
        voidBtn.addActionListener(e -> voidOrder());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        filterBar.add(smallLbl("From:")); filterBar.add(fromSpinner);
        filterBar.add(smallLbl("To:"));  filterBar.add(toSpinner);
        filterBar.add(smallLbl("Status:")); filterBar.add(statusCombo);
        filterBar.add(searchBtn);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBar.setOpaque(false);
        actionBar.add(viewBtn); actionBar.add(voidBtn);

        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);
        toolBar.add(filterBar, BorderLayout.WEST);
        toolBar.add(actionBar, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
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

        // Hide ID column
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Status colour renderer
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());
        // Right-align total column
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(7).setCellRenderer(rightAlign);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            viewBtn.setEnabled(sel);
            Order o = getSelected();
            voidBtn.setEnabled(sel && o != null && !Order.STATUS_CANCELLED.equals(o.getStatus()));
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewReceipt();
            }
        });

        JLabel countLbl = new JLabel(" ");
        countLbl.setFont(AppConfig.FONT_SMALL);
        countLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        model.addTableModelListener(ev -> countLbl.setText(model.getRowCount() + " order(s)"));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolBar,                     BorderLayout.NORTH);
        center.add(new JScrollPane(table),      BorderLayout.CENTER);
        center.add(countLbl,                    BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        loadData();
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    public void loadData() {
        try {
            LocalDate from = toLocalDate(((SpinnerDateModel) fromSpinner.getModel()).getDate());
            LocalDate to   = toLocalDate(((SpinnerDateModel) toSpinner.getModel()).getDate());
            String status  = (String) statusCombo.getSelectedItem();
            orders = AppContext.billingService().findOrders(from, to, status);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            orders = new ArrayList<>();
        }
        rebuildTable();
    }

    private void rebuildTable() {
        model.setRowCount(0);
        for (Order o : orders) {
            model.addRow(new Object[]{
                o.getId(),
                o.getOrderNumber(),
                o.getOrderType() != null ? o.getOrderType().replace("_", " ") : "—",
                o.getCustomerName() != null ? o.getCustomerName() : "Walk-in",
                o.getTableNumber() != null ? o.getTableNumber() : "—",
                o.getCashierName() != null ? o.getCashierName() : "—",
                o.getStatus(),
                String.format("₱ %.2f", o.getTotal()),
                o.getCreatedAt() != null ? o.getCreatedAt().format(DT) : "—"
            });
        }
        viewBtn.setEnabled(false); voidBtn.setEnabled(false);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void viewReceipt() {
        Order o = getSelected();
        if (o == null) return;
        try {
            Order full = AppContext.billingService().findOrderDetails(o.getId());
            new InvoiceDialog(SwingUtilities.getWindowAncestor(this), full).setVisible(true);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voidOrder() {
        Order o = getSelected();
        if (o == null) return;
        int ch = JOptionPane.showConfirmDialog(this,
                "Void order " + o.getOrderNumber() + "?\nThis cannot be undone.",
                "Confirm Void", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ch != JOptionPane.YES_OPTION) return;
        try {
            AppContext.billingService().voidOrder(o.getId());
            loadData();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Order getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return orders.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    private static LocalDate toLocalDate(java.util.Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                String s = v != null ? v.toString() : "";
                switch (s) {
                    case Order.STATUS_COMPLETED   -> setForeground(AppConfig.COLOR_SUCCESS);
                    case Order.STATUS_CANCELLED   -> setForeground(AppConfig.COLOR_ERROR);
                    case Order.STATUS_PENDING,
                         Order.STATUS_IN_PROGRESS -> setForeground(AppConfig.COLOR_WARNING);
                    default                       -> setForeground(AppConfig.COLOR_TEXT_SECONDARY);
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        };
    }

    private static JLabel smallLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppConfig.FONT_SMALL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }
}

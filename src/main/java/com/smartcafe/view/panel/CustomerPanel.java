package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Customer;
import com.smartcafe.model.Order;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {

    private static final String[] COLS = {"ID", "Name", "Phone", "Email", "Loyalty Pts", "Total Spent", "Visits", "Status"};
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final DefaultTableModel model;
    private final JTable            table;
    private final JTextField        searchField;
    private final RoundedButton     editBtn, deleteBtn, historyBtn, pointsBtn;
    private       List<Customer>    customers = new ArrayList<>();

    public CustomerPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Customer Management");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage customers, loyalty points, and purchase history.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(AppConfig.FONT_BODY);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.COLOR_BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(AppConfig.FONT_LABEL);
        // Hide ID column
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMaxWidth(0); idCol.setMinWidth(0); idCol.setPreferredWidth(0);
        // Status colour
        table.getColumnModel().getColumn(7).setCellRenderer(activeRenderer());

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (ev.getClickCount() == 2) openEdit();
            }
        });

        // Toolbar
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by name, phone, email…");
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        RoundedButton addBtn = new RoundedButton("+ Add Customer", RoundedButton.Style.PRIMARY);
        addBtn.addActionListener(e -> openAdd());
        editBtn    = new RoundedButton("✏  Edit",          RoundedButton.Style.SECONDARY);
        deleteBtn  = new RoundedButton("🗑  Delete",        RoundedButton.Style.DANGER);
        historyBtn = new RoundedButton("📋  History",       RoundedButton.Style.GHOST);
        pointsBtn  = new RoundedButton("⭐  Add Points",    RoundedButton.Style.GHOST);
        editBtn.setEnabled(false); deleteBtn.setEnabled(false);
        historyBtn.setEnabled(false); pointsBtn.setEnabled(false);

        editBtn.addActionListener(e    -> openEdit());
        deleteBtn.addActionListener(e  -> deleteCustomer());
        historyBtn.addActionListener(e -> showHistory());
        pointsBtn.addActionListener(e  -> addPoints());

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBar.setOpaque(false);
        leftBar.add(searchField);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);
        rightBar.add(pointsBtn); rightBar.add(historyBtn);
        rightBar.add(editBtn); rightBar.add(deleteBtn); rightBar.add(addBtn);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        toolbar.add(leftBar,  BorderLayout.WEST);
        toolbar.add(rightBar, BorderLayout.EAST);

        JLabel countLbl = new JLabel(" ");
        countLbl.setFont(AppConfig.FONT_SMALL);
        countLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        model.addTableModelListener(ev -> countLbl.setText(model.getRowCount() + " customer(s)"));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolbar,               BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(countLbl,              BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        try {
            customers = AppContext.customerService().findAll();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            customers = new ArrayList<>();
        }
        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (Customer c : customers) {
            if (q.isEmpty()
                || c.getFullName().toLowerCase().contains(q)
                || (c.getPhone() != null && c.getPhone().contains(q))
                || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q))) {
                model.addRow(new Object[]{
                    c.getId(),
                    c.getFullName(),
                    c.getPhone() != null ? c.getPhone() : "—",
                    c.getEmail() != null ? c.getEmail() : "—",
                    c.getLoyaltyPoints(),
                    String.format("₱ %.2f", c.getTotalSpent()),
                    c.getVisitCount(),
                    c.isActive() ? "Active" : "Inactive"
                });
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean sel = table.getSelectedRow() >= 0;
        editBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
        historyBtn.setEnabled(sel); pointsBtn.setEnabled(sel);
    }

    private Customer getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return customers.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private void openAdd() {
        new CustomerFormDialog(parentWindow(), null, this::loadData).setVisible(true);
    }

    private void openEdit() {
        Customer c = getSelected();
        if (c != null) new CustomerFormDialog(parentWindow(), c, this::loadData).setVisible(true);
    }

    private void deleteCustomer() {
        Customer c = getSelected();
        if (c == null) return;
        int ch = JOptionPane.showConfirmDialog(this,
                "Deactivate customer '" + c.getFullName() + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ch != JOptionPane.YES_OPTION) return;
        try {
            AppContext.customerService().delete(c.getId());
            loadData();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPoints() {
        Customer c = getSelected();
        if (c == null) return;
        String input = JOptionPane.showInputDialog(this,
                "Add loyalty points to " + c.getFullName() + ":", "Add Loyalty Points",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) return;
        try {
            int pts = Integer.parseInt(input.trim());
            if (pts <= 0) { JOptionPane.showMessageDialog(this, "Enter a positive number."); return; }
            AppContext.customerService().addLoyaltyPoints(c.getId(), pts);
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistory() {
        Customer c = getSelected();
        if (c == null) return;
        try {
            List<Order> orders = AppContext.customerService().getPurchaseHistory(c.getId());
            showHistoryDialog(c, orders);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistoryDialog(Customer c, List<Order> orders) {
        String[] cols = {"Order #", "Type", "Status", "Total", "Date"};
        DefaultTableModel hModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int col) { return false; }
        };
        for (Order o : orders) {
            hModel.addRow(new Object[]{
                o.getOrderNumber(),
                o.getOrderType() != null ? o.getOrderType().replace("_", " ") : "—",
                o.getStatus(),
                String.format("₱ %.2f", o.getTotal()),
                o.getCreatedAt() != null ? o.getCreatedAt().format(DT) : "—"
            });
        }
        JTable ht = new JTable(hModel);
        ht.setFont(AppConfig.FONT_BODY);
        ht.setRowHeight(32);
        ht.setShowVerticalLines(false);

        JDialog dlg = new JDialog(parentWindow(), c.getFullName() + " — Purchase History", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(700, 420);
        dlg.setLocationRelativeTo(parentWindow());
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setBackground(AppConfig.COLOR_BG);
        JLabel info = new JLabel("Points: " + c.getLoyaltyPoints() + "  |  Total Spent: ₱ " + String.format("%.2f", c.getTotalSpent()) + "  |  Visits: " + c.getVisitCount());
        info.setFont(AppConfig.FONT_BODY);
        info.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        p.add(info, BorderLayout.NORTH);
        p.add(new JScrollPane(ht), BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(close);
        p.add(btnRow, BorderLayout.SOUTH);
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private Window parentWindow() { return SwingUtilities.getWindowAncestor(this); }

    private static DefaultTableCellRenderer activeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, col);
                setForeground("Active".equals(v) ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_TEXT_HINT);
                setHorizontalAlignment(CENTER);
                return this;
            }
        };
    }
}

package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.InventoryItem;
import com.smartcafe.model.Supplier;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory management panel with two tabs:
 *   • Inventory Items — CRUD + low-stock alert banner
 *   • Suppliers       — CRUD
 */
public class InventoryPanel extends JPanel {

    // ── Inventory Items tab ───────────────────────────────────────────────────
    private static final String[] INV_COLS = {"ID", "Name", "Unit", "Stock", "Min", "Cost/Unit", "Supplier", "Status"};
    private final DefaultTableModel invModel;
    private final JTable            invTable;
    private final JTextField        invSearch;
    private final JButton           invEdit, invDelete;
    private final JLabel            alertLbl;
    private       List<InventoryItem> allItems = new ArrayList<>();

    // ── Suppliers tab ─────────────────────────────────────────────────────────
    private static final String[] SUP_COLS = {"ID", "Name", "Contact", "Phone", "Email", "Status"};
    private final DefaultTableModel supModel;
    private final JTable            supTable;
    private final JTextField        supSearch;
    private final JButton           supEdit, supDelete;
    private       List<Supplier>    allSuppliers = new ArrayList<>();

    public InventoryPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Title block ───────────────────────────────────────────────────────
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Inventory Management");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage stock levels and suppliers.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(sub);

        // ── Low-stock alert banner ────────────────────────────────────────────
        alertLbl = new JLabel();
        alertLbl.setFont(AppConfig.FONT_LABEL);
        alertLbl.setForeground(AppConfig.COLOR_WARNING);
        alertLbl.setBorder(new EmptyBorder(4, 0, 4, 0));
        alertLbl.setVisible(false);

        JPanel headerBlock = new JPanel(new BorderLayout(0, 4));
        headerBlock.setOpaque(false);
        headerBlock.setBorder(new EmptyBorder(0, 0, 12, 0));
        headerBlock.add(titleBlock, BorderLayout.NORTH);
        headerBlock.add(alertLbl,   BorderLayout.SOUTH);
        add(headerBlock, BorderLayout.NORTH);

        // ── Build table models ────────────────────────────────────────────────
        invModel = new DefaultTableModel(INV_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invTable = buildTable(invModel);

        supModel = new DefaultTableModel(SUP_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        supTable = buildTable(supModel);

        // ── Search + buttons for inventory ────────────────────────────────────
        invSearch = searchField("Search items…");
        invEdit   = new RoundedButton("✏  Edit",   RoundedButton.Style.SECONDARY);
        invDelete = new RoundedButton("🗑  Delete", RoundedButton.Style.DANGER);
        RoundedButton invAdd = new RoundedButton("+ Add Item", RoundedButton.Style.PRIMARY);
        invEdit.setEnabled(false); invDelete.setEnabled(false);

        invAdd.addActionListener(e    -> openInvForm(null));
        invEdit.addActionListener(e   -> { InventoryItem it = getSelectedItem(); if (it != null) openInvForm(it); });
        invDelete.addActionListener(e -> deleteItem());
        invSearch.getDocument().addDocumentListener(filterListener(this::applyInvFilter));

        // ── Search + buttons for suppliers ────────────────────────────────────
        supSearch = searchField("Search suppliers…");
        supEdit   = new RoundedButton("✏  Edit",   RoundedButton.Style.SECONDARY);
        supDelete = new RoundedButton("🗑  Delete", RoundedButton.Style.DANGER);
        RoundedButton supAdd = new RoundedButton("+ Add Supplier", RoundedButton.Style.PRIMARY);
        supEdit.setEnabled(false); supDelete.setEnabled(false);

        supAdd.addActionListener(e    -> openSupForm(null));
        supEdit.addActionListener(e   -> { Supplier s = getSelectedSupplier(); if (s != null) openSupForm(s); });
        supDelete.addActionListener(e -> deleteSupplier());
        supSearch.getDocument().addDocumentListener(filterListener(this::applySupFilter));

        // ── Tabs ──────────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConfig.FONT_LABEL);
        tabs.addTab("📦  Inventory Items", buildTabPanel(invSearch, invAdd, invEdit, invDelete, invTable, invModel));
        tabs.addTab("🏭  Suppliers",       buildTabPanel(supSearch, supAdd, supEdit, supDelete, supTable, supModel));
        add(tabs, BorderLayout.CENTER);

        // ── Selection listeners ───────────────────────────────────────────────
        invTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = invTable.getSelectedRow() >= 0;
            invEdit.setEnabled(sel); invDelete.setEnabled(sel);
        });
        supTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = supTable.getSelectedRow() >= 0;
            supEdit.setEnabled(sel); supDelete.setEnabled(sel);
        });
        invTable.addMouseListener(doubleClickEdit(() -> { InventoryItem it = getSelectedItem(); if (it != null) openInvForm(it); }));
        supTable.addMouseListener(doubleClickEdit(() -> { Supplier s = getSelectedSupplier(); if (s != null) openSupForm(s); }));
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    public void loadData() {
        try {
            allItems     = AppContext.inventoryService().findAll();
            allSuppliers = AppContext.supplierService().findAllActive();
        } catch (AppException ex) {
            showError(ex.getMessage());
        }
        updateAlertBanner();
        applyInvFilter();
        applySupFilter();
    }

    private void updateAlertBanner() {
        long low = allItems.stream().filter(InventoryItem::isLowStock).count();
        if (low > 0) {
            alertLbl.setText("⚠  " + low + " item" + (low == 1 ? "" : "s") + " below minimum stock level — restock needed!");
            alertLbl.setVisible(true);
        } else {
            alertLbl.setVisible(false);
        }
    }

    private void applyInvFilter() {
        String q = invSearch.getText().trim().toLowerCase();
        invModel.setRowCount(0);
        for (InventoryItem it : allItems) {
            if (q.isEmpty() || it.getName().toLowerCase().contains(q)
                    || (it.getUnit() != null && it.getUnit().toLowerCase().contains(q))
                    || (it.getSupplierName() != null && it.getSupplierName().toLowerCase().contains(q))) {
                invModel.addRow(new Object[]{
                    it.getId(), it.getName(), it.getUnit(),
                    String.format("%.2f", it.getCurrentStock()),
                    String.format("%.2f", it.getMinStock()),
                    it.getCostPerUnit() != null ? String.format("₱ %.2f", it.getCostPerUnit()) : "—",
                    it.getSupplierName() != null ? it.getSupplierName() : "—",
                    it.isLowStock() ? "⚠ Low Stock" : (it.isActive() ? "OK" : "Inactive")
                });
            }
        }
    }

    private void applySupFilter() {
        String q = supSearch.getText().trim().toLowerCase();
        supModel.setRowCount(0);
        for (Supplier s : allSuppliers) {
            if (q.isEmpty() || s.getName().toLowerCase().contains(q)
                    || (s.getContact() != null && s.getContact().toLowerCase().contains(q))
                    || (s.getEmail()   != null && s.getEmail().toLowerCase().contains(q))) {
                supModel.addRow(new Object[]{
                    s.getId(), s.getName(),
                    s.getContact() != null ? s.getContact() : "—",
                    s.getPhone()   != null ? s.getPhone()   : "—",
                    s.getEmail()   != null ? s.getEmail()   : "—",
                    s.isActive() ? "Active" : "Inactive"
                });
            }
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    private void openInvForm(InventoryItem item) {
        new InventoryItemFormDialog(parentWindow(), item, allSuppliers, this::loadData).setVisible(true);
    }

    private void openSupForm(Supplier supplier) {
        new SupplierFormDialog(parentWindow(), supplier, this::loadData).setVisible(true);
    }

    private void deleteItem() {
        InventoryItem it = getSelectedItem();
        if (it == null) return;
        int ch = JOptionPane.showConfirmDialog(this,
                "Delete item '" + it.getName() + "'?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ch == JOptionPane.YES_OPTION) {
            try {
                AppContext.inventoryService().delete(it.getId());
                loadData();
            } catch (AppException ex) { showError(ex.getMessage()); }
        }
    }

    private void deleteSupplier() {
        Supplier s = getSelectedSupplier();
        if (s == null) return;
        int ch = JOptionPane.showConfirmDialog(this,
                "Delete supplier '" + s.getName() + "'?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ch == JOptionPane.YES_OPTION) {
            try {
                AppContext.supplierService().delete(s.getId());
                loadData();
            } catch (AppException ex) { showError(ex.getMessage()); }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private InventoryItem getSelectedItem() {
        int row = invTable.getSelectedRow();
        if (row < 0) return null;
        int id = (int) invModel.getValueAt(row, 0);
        return allItems.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    private Supplier getSelectedSupplier() {
        int row = supTable.getSelectedRow();
        if (row < 0) return null;
        int id = (int) supModel.getValueAt(row, 0);
        return allSuppliers.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    private Window parentWindow() { return SwingUtilities.getWindowAncestor(this); }
    private void   showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    // ── Low-stock cell renderer for inventory table ───────────────────────────
    private static DefaultTableCellRenderer invStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                String val = v != null ? v.toString() : "";
                if (val.startsWith("⚠")) setForeground(AppConfig.COLOR_WARNING);
                else if ("OK".equals(val)) setForeground(AppConfig.COLOR_SUCCESS);
                else setForeground(AppConfig.COLOR_TEXT_HINT);
                return this;
            }
        };
    }

    private static DefaultTableCellRenderer activeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                boolean active = "Active".equals(v);
                setForeground(active ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_TEXT_HINT);
                setHorizontalAlignment(CENTER);
                return this;
            }
        };
    }

    // ── Builder helpers ───────────────────────────────────────────────────────

    private static JPanel buildTabPanel(JTextField search, JButton add,
                                         JButton edit, JButton del,
                                         JTable table, DefaultTableModel model) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppConfig.COLOR_BG);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        search.setPreferredSize(new Dimension(220, 34));
        toolbar.add(search); toolbar.add(add); toolbar.add(edit); toolbar.add(del);
        p.add(toolbar, BorderLayout.NORTH);

        JLabel count = new JLabel(" ");
        count.setFont(AppConfig.FONT_SMALL);
        count.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        model.addTableModelListener(e -> count.setText(model.getRowCount() + " record(s)"));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(count, BorderLayout.SOUTH);
        return p;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(AppConfig.FONT_BODY);
        t.setRowHeight(38);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(AppConfig.COLOR_BORDER);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(AppConfig.FONT_LABEL);
        t.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        t.getTableHeader().setForeground(AppConfig.COLOR_TEXT_SECONDARY);

        // Hide ID column
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Status column (last)
        int last = model.getColumnCount() - 1;
        if (model == invModel) {
            t.getColumnModel().getColumn(last).setCellRenderer(invStatusRenderer());
        } else {
            t.getColumnModel().getColumn(last).setCellRenderer(activeRenderer());
        }
        return t;
    }

    private static JTextField searchField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return f;
    }

    private static DocumentListener filterListener(Runnable onFilter) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onFilter.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { onFilter.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onFilter.run(); }
        };
    }

    private static java.awt.event.MouseAdapter doubleClickEdit(Runnable action) {
        return new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) action.run();
            }
        };
    }
}

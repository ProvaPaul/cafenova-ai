package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Category;
import com.smartcafe.model.Product;
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
 * Product CRUD panel — search, category filter, image thumbnails, add/edit/delete.
 *
 * Pass {@code readOnly = true} for the Cashier role so they can browse
 * products without being able to mutate them.
 */
public class ProductPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "Image", "Name", "Category", "Price", "Available", "Status"};
    private static final int      IMG_ROW_HEIGHT = 56;

    private final boolean readOnly;

    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final JTextField        searchField;
    private final JComboBox<Object> categoryFilter;
    private final JButton           editBtn;
    private final JButton           deleteBtn;

    private List<Product>  allProducts   = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();

    public ProductPanel(boolean readOnly) {
        this.readOnly = readOnly;

        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 1 ? ImageIcon.class : String.class;
            }
        };
        table = buildTable();

        // ── Search ────────────────────────────────────────────────────────────
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search products…");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // ── Category filter ───────────────────────────────────────────────────
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        categoryFilter.addActionListener(e -> applyFilter());

        // ── Buttons ───────────────────────────────────────────────────────────
        RoundedButton addBtn = new RoundedButton("+ Add Product", RoundedButton.Style.PRIMARY);
        editBtn   = new RoundedButton("✏  Edit",   RoundedButton.Style.SECONDARY);
        deleteBtn = new RoundedButton("🗑  Delete", RoundedButton.Style.DANGER);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        if (readOnly) {
            addBtn.setEnabled(false);
            addBtn.setToolTipText("View-only access");
        } else {
            addBtn.addActionListener(e    -> openAddDialog());
            editBtn.addActionListener(e   -> openEditDialog());
            deleteBtn.addActionListener(e -> deleteSelected());
        }

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Product Management");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel(readOnly ? "Browse menu items (view-only)." : "Manage menu items with prices and images.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(sub);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        searchField.setPreferredSize(new Dimension(200, 36));
        categoryFilter.setPreferredSize(new Dimension(160, 36));
        actionRow.add(searchField);
        actionRow.add(categoryFilter);
        if (!readOnly) {
            actionRow.add(addBtn);
            actionRow.add(editBtn);
            actionRow.add(deleteBtn);
        }

        toolbar.add(titleBlock, BorderLayout.WEST);
        toolbar.add(actionRow,  BorderLayout.EAST);

        // ── Row-count bar ─────────────────────────────────────────────────────
        JLabel countLbl = new JLabel(" ");
        countLbl.setFont(AppConfig.FONT_SMALL);
        countLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        countLbl.setBorder(new EmptyBorder(6, 2, 0, 0));
        tableModel.addTableModelListener(e -> countLbl.setText(tableModel.getRowCount() + " product(s)"));

        add(toolbar,                BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(countLbl,               BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = !readOnly && table.getSelectedRow() >= 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!readOnly && e.getClickCount() == 2) openEditDialog();
            }
        });

        loadData();
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public void loadData() {
        try {
            allCategories = AppContext.categoryService().findAll();
            allProducts   = AppContext.productService().findAll();
        } catch (AppException ex) {
            showError(ex.getMessage());
        }
        refreshCategoryFilter();
        applyFilter();
    }

    private void refreshCategoryFilter() {
        Object prev = categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        for (Category c : allCategories) categoryFilter.addItem(c);
        // Try to restore previous selection
        if (prev instanceof Category) {
            for (int i = 1; i < categoryFilter.getItemCount(); i++) {
                Object item = categoryFilter.getItemAt(i);
                if (item instanceof Category cat && cat.getId() == ((Category) prev).getId()) {
                    categoryFilter.setSelectedIndex(i);
                    return;
                }
            }
        }
        categoryFilter.setSelectedIndex(0);
    }

    private void applyFilter() {
        String q = searchField.getText().trim().toLowerCase();
        Integer catId = null;
        if (categoryFilter.getSelectedItem() instanceof Category c) catId = c.getId();

        tableModel.setRowCount(0);
        for (Product p : allProducts) {
            boolean matchCat = catId == null || p.getCategoryId() == catId;
            boolean matchQ   = q.isEmpty()
                    || p.getName().toLowerCase().contains(q)
                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains(q))
                    || (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(q));

            if (matchCat && matchQ) {
                tableModel.addRow(new Object[]{
                    p.getId(),
                    loadThumbnail(p.getImagePath()),
                    p.getName(),
                    p.getCategoryName() != null ? p.getCategoryName() : "—",
                    String.format("₱ %.2f", p.getPrice()),
                    p.isAvailable() ? "Yes" : "No",
                    p.isActive()    ? "Active" : "Inactive"
                });
            }
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    private void openAddDialog() {
        new ProductFormDialog(parentWindow(), null, allCategories, this::loadData).setVisible(true);
    }

    private void openEditDialog() {
        Product p = getSelectedProduct();
        if (p != null)
            new ProductFormDialog(parentWindow(), p, allCategories, this::loadData).setVisible(true);
    }

    private void deleteSelected() {
        Product p = getSelectedProduct();
        if (p == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete product '" + p.getName() + "'?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                AppContext.productService().delete(p.getId());
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Product deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                showError(ex.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        return allProducts.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private static ImageIcon loadThumbnail(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        try {
            java.io.File f = new java.io.File(imagePath);
            if (!f.exists()) return null;
            ImageIcon raw = new ImageIcon(imagePath);
            Image scaled  = raw.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) { return null; }
    }

    private Window parentWindow() { return SwingUtilities.getWindowAncestor(this); }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Table setup ───────────────────────────────────────────────────────────

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setFont(AppConfig.FONT_BODY);
        t.setRowHeight(IMG_ROW_HEIGHT);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(AppConfig.COLOR_BORDER);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(AppConfig.FONT_LABEL);
        t.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        t.getTableHeader().setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        t.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.COLOR_BORDER));

        // Hide ID column
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Image column
        t.getColumnModel().getColumn(1).setPreferredWidth(60);
        t.getColumnModel().getColumn(1).setMaxWidth(60);
        t.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel();
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof ImageIcon icon) {
                    lbl.setIcon(icon);
                } else {
                    lbl.setText("📷");
                    lbl.setForeground(AppConfig.COLOR_TEXT_HINT);
                }
                lbl.setBackground(sel ? tbl.getSelectionBackground() : tbl.getBackground());
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // Price
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        t.getColumnModel().getColumn(4).setPreferredWidth(100);
        t.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        // Available
        t.getColumnModel().getColumn(5).setPreferredWidth(80);
        t.getColumnModel().getColumn(5).setCellRenderer(availableRenderer());

        // Status
        t.getColumnModel().getColumn(6).setPreferredWidth(80);
        t.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());

        return t;
    }

    private static DefaultTableCellRenderer availableRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                boolean yes = "Yes".equals(value);
                setForeground(yes ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_TEXT_HINT);
                setText(yes ? "✔ Yes" : "✘ No");
                return this;
            }
        };
    }

    private static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                boolean active = "Active".equals(value);
                setForeground(active ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_TEXT_HINT);
                setText(active ? "● Active" : "● Inactive");
                return this;
            }
        };
    }
}

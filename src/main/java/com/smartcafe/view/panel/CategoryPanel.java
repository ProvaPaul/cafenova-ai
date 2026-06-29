package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Category;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Category CRUD panel — search, table view, add/edit/delete.
 */
public class CategoryPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "Name", "Description", "Sort", "Status", "Created"};

    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final JTextField        searchField;
    private final JButton           editBtn;
    private final JButton           deleteBtn;
    private       List<Category>    allCategories = new ArrayList<>();

    public CategoryPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table model (non-editable)
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        // Search field
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search categories…");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Action buttons
        RoundedButton addBtn = new RoundedButton("+ Add Category", RoundedButton.Style.PRIMARY);
        editBtn   = new RoundedButton("✏  Edit",   RoundedButton.Style.SECONDARY);
        deleteBtn = new RoundedButton("🗑  Delete", RoundedButton.Style.DANGER);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addActionListener(e    -> openAddDialog());
        editBtn.addActionListener(e   -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        // ── Top toolbar ───────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Category Management");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Add, edit and remove menu categories.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(sub);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        searchField.setPreferredSize(new Dimension(220, 36));
        actionRow.add(searchField);
        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        toolbar.add(titleBlock, BorderLayout.WEST);
        toolbar.add(actionRow,  BorderLayout.EAST);

        // ── Row-count bar ─────────────────────────────────────────────────────
        JLabel countLbl = new JLabel(" ");
        countLbl.setFont(AppConfig.FONT_SMALL);
        countLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        countLbl.setBorder(new EmptyBorder(6, 2, 0, 0));

        // Refresh row count when table rows change
        tableModel.addTableModelListener(e -> countLbl.setText(tableModel.getRowCount() + " record(s)"));

        add(toolbar,              BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(countLbl,             BorderLayout.SOUTH);

        // Selection → enable/disable buttons
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        // Double-click to edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        loadData();
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public void loadData() {
        try {
            allCategories = AppContext.categoryService().findAll();
        } catch (AppException ex) {
            showError(ex.getMessage());
            allCategories = new ArrayList<>();
        }
        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Category c : allCategories) {
            if (q.isEmpty()
                    || c.getName().toLowerCase().contains(q)
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q))) {

                tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getDescription() != null ? c.getDescription() : "",
                    c.getSortOrder(),
                    c.isActive() ? "Active" : "Inactive",
                    c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate().toString() : "—"
                });
            }
        }
    }

    // ── CRUD actions ──────────────────────────────────────────────────────────

    private void openAddDialog() {
        new CategoryFormDialog(parentWindow(), null, this::loadData).setVisible(true);
    }

    private void openEditDialog() {
        Category cat = getSelectedCategory();
        if (cat != null)
            new CategoryFormDialog(parentWindow(), cat, this::loadData).setVisible(true);
    }

    private void deleteSelected() {
        Category cat = getSelectedCategory();
        if (cat == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete category '" + cat.getName() + "'?\n" +
                "This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                AppContext.categoryService().delete(cat.getId());
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Category deleted successfully.", "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                showError(ex.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Category getSelectedCategory() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        return allCategories.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private Window parentWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setFont(AppConfig.FONT_BODY);
        t.setRowHeight(40);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(AppConfig.COLOR_BORDER);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(AppConfig.FONT_LABEL);
        t.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        t.getTableHeader().setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0, AppConfig.COLOR_BORDER));

        // Centre-align sort column
        DefaultTableCellRenderer centreRenderer = new DefaultTableCellRenderer();
        centreRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Hide ID column (still in model for lookups)
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setPreferredWidth(0);

        t.getColumnModel().getColumn(3).setPreferredWidth(60);
        t.getColumnModel().getColumn(3).setCellRenderer(centreRenderer);
        t.getColumnModel().getColumn(4).setPreferredWidth(80);
        t.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());
        t.getColumnModel().getColumn(5).setPreferredWidth(100);

        return t;
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

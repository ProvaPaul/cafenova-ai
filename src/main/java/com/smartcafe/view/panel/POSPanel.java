package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.*;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;

/**
 * Point-of-Sale panel — three-column layout:
 *   LEFT   : category filter buttons
 *   CENTER : scrollable product grid (card-per-product)
 *   RIGHT  : cart + order details + checkout
 */
public class POSPanel extends JPanel {

    // ── Category sidebar ───────────────────────────────────────────────────────
    private JPanel          catPanel;
    private String          activeCategoryName = null;

    // ── Product grid ───────────────────────────────────────────────────────────
    private JPanel          productGrid;
    private JTextField      productSearch;
    private List<Product>   allProducts   = new ArrayList<>();
    private List<Category>  allCategories = new ArrayList<>();

    // ── Cart table ─────────────────────────────────────────────────────────────
    private static final String[] CART_COLS = {"Product", "Price", "Qty", "Subtotal"};
    private final DefaultTableModel cartModel;
    private final JTable            cartTable;
    private final List<CartItem>    cartItems = new ArrayList<>();

    // ── Summary labels ─────────────────────────────────────────────────────────
    private final JLabel  subtotalLbl = amountLabel(false);
    private final JLabel  discountLbl = amountLabel(false);
    private final JLabel  taxLbl      = amountLabel(false);
    private final JLabel  grandTotal  = amountLabel(true);

    // ── Order detail fields ────────────────────────────────────────────────────
    private JTextField          customerField;
    private JComboBox<String>   typeCombo;
    private JComboBox<Object>   tableCombo;
    private JFormattedTextField discountField;

    // ──────────────────────────────────────────────────────────────────────────
    public POSPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout());

        // Initialise cart table first so other panels can reference cartModel/cartTable
        cartModel = new DefaultTableModel(CART_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
            @Override public Class<?> getColumnClass(int c) { return c == 2 ? Integer.class : Object.class; }
        };
        cartTable = new JTable(cartModel);
        cartModel.addTableModelListener(ev -> {
            if (ev.getColumn() != 2) return;
            int row = ev.getFirstRow();
            if (row < 0 || row >= cartItems.size()) return;
            Object val = cartModel.getValueAt(row, 2);
            if (val == null) return;
            int qty;
            try { qty = ((Number) val).intValue(); } catch (Exception e) { return; }
            CartItem ci = cartItems.get(row);
            if (qty <= 0) {
                cartItems.remove(row);
                cartModel.removeRow(row);
            } else {
                ci.setQuantity(qty);
                cartModel.setValueAt(String.format("₱ %.2f", ci.getSubtotal()), row, 3);
            }
            refreshTotals();
        });

        // ── Header bar ────────────────────────────────────────────────────────
        add(buildHeader(), BorderLayout.NORTH);

        // ── Three-column body ─────────────────────────────────────────────────
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftAndCenter(), buildCartPanel());
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(4);
        mainSplit.setResizeWeight(1.0);
        mainSplit.setDividerLocation(780);
        add(mainSplit, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(12, 0));
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(16, 20, 8, 20));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Point of Sale");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Click a product to add it to the cart • edit Qty inline • Delete key removes row");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titles.add(title); titles.add(sub);

        productSearch = new JTextField();
        productSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍  Search products…");
        productSearch.setPreferredSize(new Dimension(240, 34));
        productSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        h.add(titles,        BorderLayout.WEST);
        h.add(productSearch, BorderLayout.EAST);
        return h;
    }

    // ── Left panel: categories + product grid ─────────────────────────────────

    private JSplitPane buildLeftAndCenter() {
        catPanel = new JPanel();
        catPanel.setBackground(AppConfig.COLOR_SURFACE);
        catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.Y_AXIS));
        catPanel.setBorder(new EmptyBorder(10, 6, 10, 6));

        JScrollPane catScroll = new JScrollPane(catPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        catScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppConfig.COLOR_BORDER));

        productGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        productGrid.setBackground(AppConfig.COLOR_BG);
        productGrid.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane gridScroll = new JScrollPane(productGrid);
        gridScroll.setBorder(null);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catScroll, gridScroll);
        sp.setBorder(null);
        sp.setDividerSize(4);
        sp.setDividerLocation(150);
        return sp;
    }

    // ── Cart panel ────────────────────────────────────────────────────────────

    private JPanel buildCartPanel() {
        // Style cart table
        cartTable.setFont(AppConfig.FONT_SMALL);
        cartTable.setRowHeight(36);
        cartTable.setShowVerticalLines(false);
        cartTable.setShowHorizontalLines(true);
        cartTable.setGridColor(AppConfig.COLOR_BORDER);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(AppConfig.FONT_LABEL);
        cartTable.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        cartTable.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) removeSelectedCartRow();
            }
        });

        // Cart header + Clear
        JLabel cartTitle = new JLabel("Order Cart");
        cartTitle.setFont(AppConfig.FONT_SUBTITLE);
        cartTitle.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        RoundedButton clearBtn = new RoundedButton("Clear", RoundedButton.Style.GHOST);
        clearBtn.setFont(AppConfig.FONT_SMALL);
        clearBtn.addActionListener(e -> clearCart());

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(12, 14, 6, 14));
        hdr.add(cartTitle, BorderLayout.WEST);
        hdr.add(clearBtn,  BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, AppConfig.COLOR_BORDER));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(buildDetailsForm(), BorderLayout.NORTH);
        bottom.add(buildTotalsBlock(), BorderLayout.CENTER);
        bottom.add(buildCheckoutBar(), BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConfig.COLOR_SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, AppConfig.COLOR_BORDER));
        panel.setPreferredSize(new Dimension(310, 0));
        panel.add(hdr,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildDetailsForm() {
        customerField = new JTextField();
        customerField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Customer name (optional)");
        customerField.setFont(AppConfig.FONT_SMALL);

        typeCombo = new JComboBox<>(new String[]{
            Order.TYPE_TAKEAWAY, Order.TYPE_DINE_IN, Order.TYPE_DELIVERY});
        typeCombo.setFont(AppConfig.FONT_SMALL);
        typeCombo.addActionListener(e ->
            tableCombo.setEnabled(Order.TYPE_DINE_IN.equals(typeCombo.getSelectedItem())));

        tableCombo = new JComboBox<>();
        tableCombo.setFont(AppConfig.FONT_SMALL);
        tableCombo.setEnabled(false);
        tableCombo.addItem("No table");

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMaximumFractionDigits(2);
        discountField = new JFormattedTextField(fmt);
        discountField.setValue(0.0);
        discountField.setFont(AppConfig.FONT_SMALL);
        discountField.addPropertyChangeListener("value", ev -> refreshTotals());

        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 14, 8, 14));
        p.add(twoCol("Customer",   customerField));
        p.add(twoCol("Order Type", typeCombo));
        p.add(twoCol("Table",      tableCombo));
        p.add(twoCol("Discount %", discountField));
        return p;
    }

    private JPanel buildTotalsBlock() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppConfig.COLOR_BORDER),
                new EmptyBorder(8, 14, 4, 14)));
        p.add(faint("Subtotal"));  p.add(subtotalLbl);
        p.add(faint("Discount"));  p.add(discountLbl);
        p.add(faint("Tax 12%"));   p.add(taxLbl);
        p.add(boldLbl("TOTAL"));   p.add(grandTotal);
        return p;
    }

    private JPanel buildCheckoutBar() {
        RoundedButton btn = new RoundedButton("  Proceed to Checkout  ", RoundedButton.Style.PRIMARY);
        btn.setFont(new Font(AppConfig.FONT_BODY.getName(), Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 44));
        btn.addActionListener(e -> openPaymentDialog());
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 14, 14, 14));
        p.add(btn, BorderLayout.CENTER);
        return p;
    }

    // ── Category sidebar ──────────────────────────────────────────────────────

    private void rebuildCategorySidebar() {
        catPanel.removeAll();
        addCatBtn("All", null);
        for (Category c : allCategories) addCatBtn(c.getName(), c.getName());
        catPanel.add(Box.createVerticalGlue());
        catPanel.revalidate(); catPanel.repaint();
    }

    private void addCatBtn(String label, String catName) {
        boolean active = Objects.equals(catName, activeCategoryName);
        JButton b = new JButton(label);
        b.setFont(AppConfig.FONT_SMALL);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBackground(active ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_SURFACE);
        b.setForeground(active ? Color.WHITE : AppConfig.COLOR_TEXT_PRIMARY);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.addActionListener(e -> {
            activeCategoryName = catName;
            rebuildCategorySidebar();
            applyFilter();
        });
        catPanel.add(b);
        catPanel.add(Box.createVerticalStrut(4));
    }

    // ── Product grid ──────────────────────────────────────────────────────────

    private void rebuildProductGrid(List<Product> products) {
        productGrid.removeAll();
        if (products.isEmpty()) {
            productGrid.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No products found.", SwingConstants.CENTER);
            empty.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
            empty.setFont(AppConfig.FONT_BODY);
            productGrid.add(empty, BorderLayout.CENTER);
        } else {
            productGrid.setLayout(new GridLayout(0, 3, 10, 10));
            for (Product p : products) productGrid.add(buildProductCard(p));
        }
        productGrid.revalidate(); productGrid.repaint();
    }

    private JPanel buildProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(AppConfig.COLOR_SURFACE);
        card.setBorder(cardBorder(false));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel imgLbl;
        if (p.getImagePath() != null && !p.getImagePath().isBlank()) {
            ImageIcon icon = loadIcon(p.getImagePath(), 90, 70);
            imgLbl = icon != null ? new JLabel(icon, SwingConstants.CENTER) : coffeeIcon();
        } else {
            imgLbl = coffeeIcon();
        }
        imgLbl.setPreferredSize(new Dimension(0, 70));

        JLabel nameLbl = new JLabel("<html><b>" + htmlEsc(p.getName()) + "</b></html>",
                SwingConstants.CENTER);
        nameLbl.setFont(AppConfig.FONT_SMALL);
        nameLbl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        JLabel priceLbl = new JLabel("₱ " + String.format("%.2f", p.getPrice()),
                SwingConstants.CENTER);
        priceLbl.setFont(AppConfig.FONT_LABEL);
        priceLbl.setForeground(AppConfig.COLOR_ACCENT);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        info.add(nameLbl); info.add(priceLbl);

        card.add(imgLbl, BorderLayout.CENTER);
        card.add(info,   BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { addToCart(p); }
            @Override public void mouseEntered(MouseEvent e) { card.setBorder(cardBorder(true)); }
            @Override public void mouseExited(MouseEvent e)  { card.setBorder(cardBorder(false)); }
        });
        return card;
    }

    // ── Cart operations ───────────────────────────────────────────────────────

    private void addToCart(Product p) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProduct().getId() == p.getId()) {
                CartItem ci = cartItems.get(i);
                ci.setQuantity(ci.getQuantity() + 1);
                cartModel.setValueAt(ci.getQuantity(), i, 2);
                cartModel.setValueAt(String.format("₱ %.2f", ci.getSubtotal()), i, 3);
                refreshTotals();
                return;
            }
        }
        CartItem ci = new CartItem(p, 1);
        cartItems.add(ci);
        cartModel.addRow(new Object[]{
            p.getName(),
            String.format("₱ %.2f", p.getPrice()),
            1,
            String.format("₱ %.2f", ci.getSubtotal())
        });
        refreshTotals();
    }

    private void removeSelectedCartRow() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            cartModel.removeRow(row);
            refreshTotals();
        }
    }

    private void clearCart() {
        cartItems.clear();
        cartModel.setRowCount(0);
        refreshTotals();
    }

    private void refreshTotals() {
        double sub     = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        double disc    = getDiscountPct();
        double discAmt = Math.round(sub * disc / 100.0 * 100) / 100.0;
        double taxable = sub - discAmt;
        double tax     = Math.round(taxable * 0.12 * 100) / 100.0;
        double total   = Math.round((taxable + tax) * 100) / 100.0;

        subtotalLbl.setText("₱ " + String.format("%.2f", sub));
        discountLbl.setText("₱ " + String.format("%.2f", discAmt));
        taxLbl.setText("₱ " + String.format("%.2f", tax));
        grandTotal.setText("₱ " + String.format("%.2f", total));
    }

    private double getDiscountPct() {
        Object v = discountField.getValue();
        if (v == null) return 0;
        try { return Math.min(100, Math.max(0, ((Number) v).doubleValue())); }
        catch (Exception e) { return 0; }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    public void loadData() {
        try {
            allProducts   = AppContext.productService().findAll();
            allCategories = AppContext.categoryService().findAll();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        rebuildCategorySidebar();
        applyFilter();
    }

    private void applyFilter() {
        String q   = productSearch.getText().trim().toLowerCase();
        String cat = activeCategoryName;
        List<Product> visible = new ArrayList<>();
        for (Product p : allProducts) {
            if (!p.isActive()) continue;
            if (cat != null && !cat.equals(p.getCategoryName())) continue;
            if (!q.isEmpty() && !p.getName().toLowerCase().contains(q)) continue;
            visible.add(p);
        }
        rebuildProductGrid(visible);
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    private void openPaymentDialog() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty — add at least one product.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double sub     = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        double disc    = getDiscountPct();
        double discAmt = Math.round(sub * disc / 100.0 * 100) / 100.0;
        double taxable = sub - discAmt;
        double tax     = Math.round(taxable * 0.12 * 100) / 100.0;
        double total   = Math.round((taxable + tax) * 100) / 100.0;

        Integer tableId = null;
        if (tableCombo.getSelectedItem() instanceof CafeTable t) tableId = t.getId();

        String orderType = typeCombo.getSelectedItem() != null
                ? (String) typeCombo.getSelectedItem() : Order.TYPE_TAKEAWAY;

        new PaymentDialog(
            SwingUtilities.getWindowAncestor(this),
            new ArrayList<>(cartItems),
            tableId, orderType,
            customerField.getText().trim(),
            disc, sub, discAmt, tax, total,
            completedOrder -> {
                clearCart();
                customerField.setText("");
                discountField.setValue(0.0);
                typeCombo.setSelectedIndex(0);
                new InvoiceDialog(
                    SwingUtilities.getWindowAncestor(this), completedOrder
                ).setVisible(true);
            }
        ).setVisible(true);
    }

    // ── Small helpers ─────────────────────────────────────────────────────────

    private static JPanel twoCol(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label + ":");
        l.setFont(AppConfig.FONT_SMALL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(82, 0));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private static JLabel faint(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppConfig.FONT_SMALL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }

    private static JLabel boldLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(AppConfig.FONT_LABEL.getName(), Font.BOLD, AppConfig.FONT_LABEL.getSize()));
        l.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        return l;
    }

    private static JLabel amountLabel(boolean big) {
        JLabel l = new JLabel("₱ 0.00", SwingConstants.RIGHT);
        l.setFont(big
                ? new Font(AppConfig.FONT_TITLE.getName(), Font.BOLD, 16)
                : AppConfig.FONT_SMALL);
        l.setForeground(big ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_TEXT_PRIMARY);
        return l;
    }

    private static JLabel coffeeIcon() {
        JLabel l = new JLabel("☕", SwingConstants.CENTER);
        l.setFont(new Font("Dialog", Font.PLAIN, 28));
        l.setForeground(AppConfig.COLOR_TEXT_HINT);
        return l;
    }

    private static Border cardBorder(boolean hover) {
        Color c = hover ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_BORDER;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c),
                new EmptyBorder(8, 8, 8, 8));
    }

    private static ImageIcon loadIcon(String path, int w, int h) {
        try {
            ImageIcon raw = new ImageIcon(path);
            if (raw.getIconWidth() <= 0) return null;
            return new ImageIcon(raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    private static String htmlEsc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

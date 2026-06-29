package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.*;
import com.smartcafe.view.components.RoundedButton;
import com.smartcafe.util.SessionManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Payment dialog — shown after clicking "Proceed to Checkout".
 * Displays order summary, payment method selection, amount paid,
 * change calculation, and the "Place Order" button.
 */
public class PaymentDialog extends JDialog {

    private final List<CartItem>    cartItems;
    private final Integer           tableId;
    private final String            orderType;
    private final String            customerName;
    private final double            discountPct;
    private final double            subtotal;
    private final double            discountAmt;
    private final double            tax;
    private final double            total;
    private final Consumer<Order>   onSuccess;

    private ButtonGroup             methodGroup;
    private JFormattedTextField     amountPaidField;
    private JLabel                  changeLbl;

    public PaymentDialog(Window owner, List<CartItem> cartItems,
                         Integer tableId, String orderType, String customerName,
                         double discountPct, double subtotal, double discountAmt,
                         double tax, double total, Consumer<Order> onSuccess) {
        super(owner, "Payment — " + String.format("₱ %.2f", total), ModalityType.APPLICATION_MODAL);
        this.cartItems    = cartItems;
        this.tableId      = tableId;
        this.orderType    = orderType;
        this.customerName = customerName.isBlank() ? null : customerName;
        this.discountPct  = discountPct;
        this.subtotal     = subtotal;
        this.discountAmt  = discountAmt;
        this.tax          = tax;
        this.total        = total;
        this.onSuccess    = onSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        setSize(460, 540);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppConfig.COLOR_SURFACE);
        setContentPane(root);

        // ── Summary ───────────────────────────────────────────────────────────
        JPanel summary = new JPanel(new GridLayout(0, 2, 8, 6));
        summary.setOpaque(false);
        summary.setBorder(new EmptyBorder(20, 24, 12, 24));
        addSummaryRow(summary, "Subtotal",   String.format("₱ %.2f", subtotal),  false);
        if (discountAmt > 0)
            addSummaryRow(summary, "Discount", String.format("- ₱ %.2f", discountAmt), false);
        addSummaryRow(summary, "Tax (12%)", String.format("₱ %.2f", tax),        false);
        addSummaryRow(summary, "TOTAL DUE", String.format("₱ %.2f", total),      true);

        // ── Method ────────────────────────────────────────────────────────────
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        methodPanel.setOpaque(false);
        methodPanel.setBorder(new TitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.COLOR_BORDER),
                "  Payment Method  "));
        methodGroup = new ButtonGroup();
        for (String method : new String[]{Payment.METHOD_CASH, Payment.METHOD_CARD, Payment.METHOD_MOBILE}) {
            JRadioButton rb = new JRadioButton(method.replace("_", " "));
            rb.setActionCommand(method);
            rb.setFont(AppConfig.FONT_BODY);
            rb.setOpaque(false);
            if (Payment.METHOD_CASH.equals(method)) rb.setSelected(true);
            methodGroup.add(rb);
            methodPanel.add(rb);
        }

        // ── Amount paid ───────────────────────────────────────────────────────
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        amountPaidField = new JFormattedTextField(fmt);
        amountPaidField.setValue(total);
        amountPaidField.setFont(new Font(AppConfig.FONT_BODY.getName(), Font.BOLD, 16));
        amountPaidField.setHorizontalAlignment(JTextField.RIGHT);
        amountPaidField.setPreferredSize(new Dimension(0, 44));
        amountPaidField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0.00");
        amountPaidField.addPropertyChangeListener("value", ev -> updateChange());

        changeLbl = new JLabel("Change: ₱ 0.00", SwingConstants.RIGHT);
        changeLbl.setFont(new Font(AppConfig.FONT_TITLE.getName(), Font.BOLD, 18));
        changeLbl.setForeground(AppConfig.COLOR_SUCCESS);
        updateChange();

        JPanel amountPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        amountPanel.setOpaque(false);
        amountPanel.setBorder(new EmptyBorder(10, 24, 10, 24));
        amountPanel.add(label("Amount Received (₱):"));
        amountPanel.add(amountPaidField);
        amountPanel.add(changeLbl);

        // ── Buttons ───────────────────────────────────────────────────────────
        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.GHOST);
        cancelBtn.addActionListener(e -> dispose());
        RoundedButton placeBtn = new RoundedButton("  Place Order  ", RoundedButton.Style.PRIMARY);
        placeBtn.setFont(new Font(AppConfig.FONT_BODY.getName(), Font.BOLD, 14));
        placeBtn.addActionListener(e -> placeOrder());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(6, 24, 20, 24));
        btnPanel.add(cancelBtn);
        btnPanel.add(placeBtn);

        JPanel methodWrap = new JPanel(new BorderLayout());
        methodWrap.setOpaque(false);
        methodWrap.setBorder(new EmptyBorder(0, 24, 0, 24));
        methodWrap.add(methodPanel);

        root.add(summary,     BorderLayout.NORTH);
        root.add(methodWrap,  BorderLayout.CENTER);

        JPanel lowerBlock = new JPanel(new BorderLayout());
        lowerBlock.setOpaque(false);
        lowerBlock.add(amountPanel, BorderLayout.CENTER);
        lowerBlock.add(btnPanel,    BorderLayout.SOUTH);
        root.add(lowerBlock, BorderLayout.SOUTH);
    }

    private void updateChange() {
        Object v = amountPaidField.getValue();
        double paid = v instanceof Number ? ((Number) v).doubleValue() : 0;
        double change = Math.max(0, Math.round((paid - total) * 100) / 100.0);
        changeLbl.setText(String.format("Change: ₱ %.2f", change));
        changeLbl.setForeground(paid >= total ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_WARNING);
    }

    private void placeOrder() {
        String method = methodGroup.getSelection() != null
                ? methodGroup.getSelection().getActionCommand() : Payment.METHOD_CASH;

        Object v = amountPaidField.getValue();
        double paid = v instanceof Number ? ((Number) v).doubleValue() : 0;

        if (Payment.METHOD_CASH.equals(method) && paid < total) {
            JOptionPane.showMessageDialog(this,
                    String.format("Amount paid (₱ %.2f) is less than the total (₱ %.2f).", paid, total),
                    "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cashierId = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getId() : 1;

        try {
            Order order = AppContext.orderService().placeOrder(
                    cartItems, tableId, orderType, customerName,
                    null, discountPct, method, paid, cashierId);
            dispose();
            onSuccess.accept(order);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addSummaryRow(JPanel p, String label, String value, boolean bold) {
        JLabel l = new JLabel(label);
        l.setFont(bold
                ? new Font(AppConfig.FONT_LABEL.getName(), Font.BOLD, 13)
                : AppConfig.FONT_LABEL);
        l.setForeground(bold ? AppConfig.COLOR_TEXT_PRIMARY : AppConfig.COLOR_TEXT_SECONDARY);

        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(bold
                ? new Font(AppConfig.FONT_TITLE.getName(), Font.BOLD, 15)
                : AppConfig.FONT_BODY);
        v.setForeground(bold ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_TEXT_PRIMARY);
        p.add(l); p.add(v);
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_LABEL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }
}

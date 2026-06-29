package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.model.Order;
import com.smartcafe.model.OrderItem;
import com.smartcafe.model.Payment;
import com.smartcafe.util.PdfInvoiceUtil;
import com.smartcafe.util.QrCodeUtil;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Receipt dialog shown immediately after a successful order placement.
 * Displays a thermal-receipt-style preview with QR code, plus buttons
 * for Save PDF and Print.
 */
public class InvoiceDialog extends JDialog {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a");
    private static final int QR_SIZE = 110;

    private final Order order;

    public InvoiceDialog(Window owner, Order order) {
        super(owner, "Receipt — " + order.getOrderNumber(), ModalityType.APPLICATION_MODAL);
        this.order = order;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setMinimumSize(new Dimension(420, 500));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppConfig.COLOR_SURFACE);
        setContentPane(root);

        // ── Receipt preview ───────────────────────────────────────────────────
        JPanel receipt = buildReceiptPanel();
        JScrollPane scroll = new JScrollPane(receipt);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(scroll, BorderLayout.CENTER);

        // ── Action buttons ────────────────────────────────────────────────────
        RoundedButton closeBtn = new RoundedButton("Close",      RoundedButton.Style.GHOST);
        RoundedButton pdfBtn   = new RoundedButton("Save PDF",   RoundedButton.Style.SECONDARY);
        RoundedButton printBtn = new RoundedButton("🖨 Print",   RoundedButton.Style.PRIMARY);

        closeBtn.addActionListener(e -> dispose());
        pdfBtn.addActionListener(e   -> savePdf());
        printBtn.addActionListener(e -> printReceipt());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(AppConfig.COLOR_SURFACE);
        btns.setBorder(new EmptyBorder(10, 16, 14, 16));
        btns.add(closeBtn); btns.add(pdfBtn); btns.add(printBtn);
        root.add(btns, BorderLayout.SOUTH);
    }

    private JPanel buildReceiptPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        p.add(centredBold("SMART CAFE", 16));
        p.add(centred("Smart AI-Based Cafe Management System", 11));
        p.add(centred("123 Coffee St., Manila, Philippines", 11));
        p.add(gap(6));
        p.add(centredBold("OFFICIAL RECEIPT", 12));
        p.add(divider());

        p.add(row("Order #", order.getOrderNumber()));
        if (order.getCreatedAt() != null)
            p.add(row("Date", order.getCreatedAt().format(DT)));
        if (order.getCashierName() != null)
            p.add(row("Cashier", order.getCashierName()));
        p.add(row("Type", order.getOrderType() != null
                ? order.getOrderType().replace("_", " ") : "—"));
        if (order.getTableNumber() != null)
            p.add(row("Table", order.getTableNumber()));
        if (order.getCustomerName() != null)
            p.add(row("Customer", order.getCustomerName()));
        p.add(divider());

        // Items header
        p.add(itemRow("Item", "Qty", "Price", "Total", true));
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                p.add(itemRow(
                    item.getProductName(),
                    String.valueOf(item.getQuantity()),
                    String.format("%.2f", item.getUnitPrice()),
                    String.format("%.2f", item.getSubtotal()),
                    false));
            }
        }
        p.add(divider());

        p.add(row("Subtotal",  String.format("₱ %.2f", order.getSubtotal())));
        if (order.getDiscount() > 0)
            p.add(row("Discount", String.format("- ₱ %.2f", order.getDiscount())));
        p.add(row("Tax (12%)", String.format("₱ %.2f", order.getTax())));
        p.add(gap(4));
        p.add(rowBold("TOTAL DUE", String.format("₱ %.2f", order.getTotal())));
        p.add(divider());

        Payment pay = order.getPayment();
        if (pay != null) {
            p.add(row("Method",  pay.getPaymentMethod() != null
                    ? pay.getPaymentMethod().replace("_", " ") : "—"));
            p.add(row("Amount Paid", String.format("₱ %.2f", pay.getAmountPaid())));
            if (pay.getChangeAmount() > 0)
                p.add(row("Change", String.format("₱ %.2f", pay.getChangeAmount())));
            if (pay.getTransactionRef() != null)
                p.add(row("Ref #", pay.getTransactionRef()));
        }
        p.add(divider());

        // QR Code
        String qrContent = "Order:" + order.getOrderNumber()
                + "|Total:₱" + String.format("%.2f", order.getTotal());
        BufferedImage qrImg = QrCodeUtil.generate(qrContent, QR_SIZE);
        if (qrImg != null) {
            JLabel qrLbl = new JLabel(new ImageIcon(qrImg));
            qrLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(gap(6));
            p.add(qrLbl);
            p.add(gap(4));
            p.add(centred("Scan to verify order", 10));
        }
        p.add(gap(10));
        p.add(centred("Thank you for visiting Smart Cafe!", 11));
        return p;
    }

    private void savePdf() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(order.getOrderNumber() + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File target = fc.getSelectedFile();
        if (!target.getName().endsWith(".pdf")) target = new File(target.getPath() + ".pdf");
        try {
            PdfInvoiceUtil.savePdf(order, target);
            JOptionPane.showMessageDialog(this,
                    "Invoice saved to:\n" + target.getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "PDF export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReceipt() {
        try {
            PdfInvoiceUtil.print(order);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Print failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Receipt widget builders ───────────────────────────────────────────────

    private static JLabel centredBold(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Dialog", Font.BOLD, size));
        l.setForeground(Color.BLACK);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 4));
        return l;
    }

    private static JLabel centred(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Dialog", Font.PLAIN, size));
        l.setForeground(Color.DARK_GRAY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 2));
        return l;
    }

    private static JPanel divider() {
        JPanel d = new JPanel();
        d.setBackground(Color.WHITE);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        d.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        return d;
    }

    private static Component gap(int h) {
        return Box.createVerticalStrut(h);
    }

    private static JPanel row(String label, String value) {
        return rowImpl(label, value, false);
    }

    private static JPanel rowBold(String label, String value) {
        return rowImpl(label, value, true);
    }

    private static JPanel rowImpl(String label, String value, boolean bold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel l = new JLabel(label);
        l.setFont(bold
                ? new Font("Dialog", Font.BOLD, 11)
                : new Font("Dialog", Font.PLAIN, 11));
        l.setForeground(bold ? Color.BLACK : Color.DARK_GRAY);

        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(bold
                ? new Font("Dialog", Font.BOLD, 13)
                : new Font("Dialog", Font.PLAIN, 11));
        v.setForeground(bold ? new Color(0x6B3F2A) : Color.BLACK);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private static JPanel itemRow(String name, String qty, String price,
                                  String total, boolean header) {
        JPanel p = new JPanel(new GridLayout(1, 4, 4, 0));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        Font f = header
                ? new Font("Dialog", Font.BOLD, 10)
                : new Font("Dialog", Font.PLAIN, 10);
        for (String[] pair : new String[][]{{name, "LEFT"}, {qty, "RIGHT"}, {price, "RIGHT"}, {total, "RIGHT"}}) {
            JLabel l = new JLabel(pair[0],
                    "RIGHT".equals(pair[1]) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setFont(f);
            l.setForeground(Color.BLACK);
            p.add(l);
        }
        return p;
    }
}

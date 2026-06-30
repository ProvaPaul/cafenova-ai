package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerFormDialog extends JDialog {

    private final Customer customer;
    private final Runnable onSave;

    private final JTextField nameField  = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextArea  addrField  = new JTextArea(3, 20);
    private final JCheckBox  activeBox  = new JCheckBox("Active", true);

    public CustomerFormDialog(Window owner, Customer customer, Runnable onSave) {
        super(owner, customer == null ? "Add Customer" : "Edit Customer",
              ModalityType.APPLICATION_MODAL);
        this.customer = customer;
        this.onSave   = onSave;
        buildUI();
        if (customer != null) populate();
        setSize(420, 380);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        root.setBackground(AppConfig.COLOR_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(4,0,4,8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1; fc.insets = new Insets(4,0,4,0);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        addrField.setRows(3);
        addrField.setLineWrap(true);
        addrField.setWrapStyleWord(true);

        addRow(form, lc, fc, 0, "Full Name *", nameField);
        addRow(form, lc, fc, 1, "Phone",       phoneField);
        addRow(form, lc, fc, 2, "Email",        emailField);

        lc.gridy = 3; fc.gridy = 3;
        form.add(lbl("Address"), lc);
        fc.fill = GridBagConstraints.BOTH; fc.weighty = 1;
        form.add(new JScrollPane(addrField), fc);
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weighty = 0;

        lc.gridy = 4; fc.gridy = 4;
        form.add(new JLabel(""), lc);
        form.add(activeBox, fc);
        activeBox.setOpaque(false);
        activeBox.setForeground(AppConfig.COLOR_TEXT_PRIMARY);

        root.add(form, BorderLayout.CENTER);

        JButton save   = new JButton(customer == null ? "Add Customer" : "Save Changes");
        JButton cancel = new JButton("Cancel");
        save.setBackground(AppConfig.COLOR_PRIMARY);
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> doSave());
        cancel.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(cancel); btns.add(save);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void populate() {
        nameField.setText(customer.getFullName());
        phoneField.setText(customer.getPhone() != null ? customer.getPhone() : "");
        emailField.setText(customer.getEmail() != null ? customer.getEmail() : "");
        addrField.setText(customer.getAddress() != null ? customer.getAddress() : "");
        activeBox.setSelected(customer.isActive());
    }

    private void doSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name is required.");
            return;
        }
        try {
            if (customer == null) {
                Customer c = new Customer(name,
                    emptyToNull(phoneField.getText()),
                    emptyToNull(emailField.getText()),
                    emptyToNull(addrField.getText()));
                AppContext.customerService().save(c);
            } else {
                customer.setFullName(name);
                customer.setPhone(emptyToNull(phoneField.getText()));
                customer.setEmail(emptyToNull(emailField.getText()));
                customer.setAddress(emptyToNull(addrField.getText()));
                customer.setActive(activeBox.isSelected());
                AppContext.customerService().update(customer);
            }
            onSave.run();
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addRow(JPanel p, GridBagConstraints lc, GridBagConstraints fc,
                                int row, String label, JComponent field) {
        lc.gridy = row; fc.gridy = row;
        p.add(lbl(label), lc);
        p.add(field, fc);
    }

    private static JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_LABEL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}

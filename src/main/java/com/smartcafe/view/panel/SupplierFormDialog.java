package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Supplier;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SupplierFormDialog extends JDialog {

    private final Supplier existing;
    private final Runnable onSuccess;

    private JTextField nameField, contactField, phoneField, emailField;
    private JTextArea  addressArea;
    private JCheckBox  activeBox;

    public SupplierFormDialog(Window owner, Supplier supplier, Runnable onSuccess) {
        super(owner, supplier == null ? "Add Supplier" : "Edit Supplier — " + supplier.getName(),
              ModalityType.APPLICATION_MODAL);
        this.existing  = supplier;
        this.onSuccess = onSuccess;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        if (existing != null) populate();
        pack();
        setMinimumSize(new Dimension(420, 0));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConfig.COLOR_SURFACE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppConfig.COLOR_SURFACE);
        form.setBorder(new EmptyBorder(22, 26, 8, 26));

        GridBagConstraints lc = gbc(0, GridBagConstraints.NONE);
        GridBagConstraints fc = gbc(1, GridBagConstraints.HORIZONTAL);
        fc.weightx = 1;

        nameField    = field("e.g. Premium Coffee Co.");
        contactField = field("Contact person name");
        phoneField   = field("+63 9XX XXX XXXX");
        emailField   = field("supplier@example.com");

        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true); addressArea.setWrapStyleWord(true);
        addressArea.setFont(AppConfig.FONT_BODY);
        addressArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Street, City, Province");
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setPreferredSize(new Dimension(0, 70));

        activeBox = new JCheckBox("Active"); activeBox.setSelected(true);
        activeBox.setFont(AppConfig.FONT_BODY); activeBox.setOpaque(false);

        addRow(form, lc, fc, 0, "Name *",         nameField);
        addRow(form, lc, fc, 1, "Contact Person",  contactField);
        addRow(form, lc, fc, 2, "Phone",           phoneField);
        addRow(form, lc, fc, 3, "Email",           emailField);
        addRow(form, lc, fc, 4, "Address",         addrScroll);
        if (existing != null) addRow(form, lc, fc, 5, "Status", activeBox);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(AppConfig.COLOR_SURFACE);
        btns.setBorder(new EmptyBorder(10, 26, 18, 26));
        RoundedButton cancel = new RoundedButton("Cancel", RoundedButton.Style.GHOST);
        cancel.addActionListener(e -> dispose());
        RoundedButton save = new RoundedButton(existing == null ? "Add Supplier" : "Save Changes",
                RoundedButton.Style.PRIMARY);
        save.addActionListener(e -> save());
        btns.add(cancel); btns.add(save);

        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void populate() {
        nameField.setText(existing.getName());
        contactField.setText(nvl(existing.getContact()));
        phoneField.setText(nvl(existing.getPhone()));
        emailField.setText(nvl(existing.getEmail()));
        addressArea.setText(nvl(existing.getAddress()));
        activeBox.setSelected(existing.isActive());
    }

    private void save() {
        try {
            String name    = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String phone   = phoneField.getText().trim();
            String email   = emailField.getText().trim();
            String address = addressArea.getText().trim();

            if (existing == null) {
                AppContext.supplierService().create(name, contact, phone, email, address);
                JOptionPane.showMessageDialog(this, "Supplier added.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                existing.setName(name);
                existing.setContact(contact.isBlank() ? null : contact);
                existing.setPhone(phone.isBlank()     ? null : phone);
                existing.setEmail(email.isBlank()     ? null : email);
                existing.setAddress(address.isBlank() ? null : address);
                existing.setActive(activeBox.isSelected());
                AppContext.supplierService().update(existing);
                JOptionPane.showMessageDialog(this, "Supplier updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            onSuccess.run();
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField field(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));
        return f;
    }

    private static void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc,
                                int row, String label, JComponent field) {
        lc.gridy = row; fc.gridy = row;
        lc.insets = new Insets(row == 0 ? 0 : 10, 0, 0, 14);
        fc.insets = new Insets(row == 0 ? 0 : 10, 0, 0, 0);
        JLabel l = new JLabel(label);
        l.setFont(AppConfig.FONT_LABEL); l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        form.add(l, lc); form.add(field, fc);
    }

    private static GridBagConstraints gbc(int x, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.fill = fill; c.anchor = GridBagConstraints.WEST; return c;
    }

    private static String nvl(String s) { return s != null ? s : ""; }
}

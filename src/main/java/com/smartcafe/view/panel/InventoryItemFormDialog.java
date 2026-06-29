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
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InventoryItemFormDialog extends JDialog {

    private final InventoryItem existing;
    private final List<Supplier> suppliers;
    private final Runnable onSuccess;

    private JTextField          nameField;
    private JComboBox<String>   unitCombo;
    private JFormattedTextField stockField, minField, costField;
    private JComboBox<Object>   supplierCombo;
    private JCheckBox           activeBox;

    private static final String[] UNITS =
        {"kg", "g", "litre", "ml", "piece", "box", "bottle", "bag", "pack", "dozen", "other"};

    public InventoryItemFormDialog(Window owner, InventoryItem item,
                                   List<Supplier> suppliers, Runnable onSuccess) {
        super(owner, item == null ? "Add Inventory Item" : "Edit — " + item.getName(),
              ModalityType.APPLICATION_MODAL);
        this.existing  = item;
        this.suppliers = suppliers;
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

        GridBagConstraints lc = gbc(0, 0, GridBagConstraints.NONE);
        GridBagConstraints fc = gbc(1, 0, GridBagConstraints.HORIZONTAL);
        fc.weightx = 1;

        nameField = new JTextField();
        nameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Coffee Beans");
        nameField.setPreferredSize(new Dimension(240, AppConfig.FIELD_HEIGHT));

        unitCombo = new JComboBox<>(UNITS);
        unitCombo.setEditable(true);

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        stockField = new JFormattedTextField(fmt); stockField.setValue(0.0);
        stockField.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));
        minField   = new JFormattedTextField(fmt); minField.setValue(0.0);
        minField.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));
        costField  = new JFormattedTextField(fmt); costField.setValue(0.0);
        costField.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));

        supplierCombo = new JComboBox<>();
        supplierCombo.addItem("None");
        for (Supplier s : suppliers) supplierCombo.addItem(s);
        supplierCombo.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));

        activeBox = new JCheckBox("Active"); activeBox.setSelected(true);
        activeBox.setFont(AppConfig.FONT_BODY); activeBox.setOpaque(false);

        addRow(form, lc, fc, 0, "Name *",         nameField);
        addRow(form, lc, fc, 1, "Unit *",          unitCombo);
        addRow(form, lc, fc, 2, "Current Stock",   stockField);
        addRow(form, lc, fc, 3, "Min Stock Level", minField);
        addRow(form, lc, fc, 4, "Cost per Unit",   costField);
        addRow(form, lc, fc, 5, "Supplier",        supplierCombo);
        if (existing != null) addRow(form, lc, fc, 6, "Status", activeBox);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(AppConfig.COLOR_SURFACE);
        btns.setBorder(new EmptyBorder(10, 26, 18, 26));
        RoundedButton cancel = new RoundedButton("Cancel", RoundedButton.Style.GHOST);
        cancel.addActionListener(e -> dispose());
        RoundedButton save = new RoundedButton(existing == null ? "Add Item" : "Save Changes",
                RoundedButton.Style.PRIMARY);
        save.addActionListener(e -> save());
        btns.add(cancel); btns.add(save);

        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void populate() {
        nameField.setText(existing.getName());
        unitCombo.setSelectedItem(existing.getUnit());
        stockField.setValue(existing.getCurrentStock());
        minField.setValue(existing.getMinStock());
        costField.setValue(existing.getCostPerUnit() != null ? existing.getCostPerUnit() : 0.0);
        activeBox.setSelected(existing.isActive());
        if (existing.getSupplierId() != null) {
            for (int i = 1; i < supplierCombo.getItemCount(); i++) {
                Object obj = supplierCombo.getItemAt(i);
                if (obj instanceof Supplier s && s.getId() == existing.getSupplierId()) {
                    supplierCombo.setSelectedIndex(i); break;
                }
            }
        }
    }

    private void save() {
        try {
            String name  = nameField.getText().trim();
            String unit  = supplierCombo.getSelectedItem() instanceof String ? "piece"
                    : unitCombo.getSelectedItem() != null ? unitCombo.getSelectedItem().toString().trim() : "";
            unit = unitCombo.getSelectedItem() != null ? unitCombo.getSelectedItem().toString().trim() : "";
            double stock = ((Number) stockField.getValue()).doubleValue();
            double min   = ((Number) minField.getValue()).doubleValue();
            double cost  = ((Number) costField.getValue()).doubleValue();
            Double costPU = cost > 0 ? cost : null;
            Integer supId = supplierCombo.getSelectedItem() instanceof Supplier s ? s.getId() : null;

            if (existing == null) {
                AppContext.inventoryService().create(name, unit, stock, min, costPU, supId);
                JOptionPane.showMessageDialog(this, "Item added.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                existing.setName(name);
                existing.setUnit(unit);
                existing.setCurrentStock(stock);
                existing.setMinStock(min);
                existing.setCostPerUnit(costPU);
                existing.setSupplierId(supId);
                existing.setActive(activeBox.isSelected());
                AppContext.inventoryService().update(existing);
                JOptionPane.showMessageDialog(this, "Item updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            onSuccess.run();
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc,
                                int row, String label, JComponent field) {
        lc.gridy = row; fc.gridy = row;
        lc.insets = new Insets(row == 0 ? 0 : 10, 0, 0, 14);
        fc.insets = new Insets(row == 0 ? 0 : 10, 0, 0, 0);
        JLabel l = new JLabel(label);
        l.setFont(AppConfig.FONT_LABEL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        form.add(l, lc); form.add(field, fc);
    }

    private static GridBagConstraints gbc(int x, int y, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.fill = fill;
        c.anchor = GridBagConstraints.WEST; return c;
    }
}

package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Category;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for creating or editing a {@link Category}.
 * Pass {@code null} for {@code category} to open in Add mode.
 */
public class CategoryFormDialog extends JDialog {

    private final Category existing;
    private final Runnable onSuccess;

    private JTextField nameField;
    private JTextArea  descArea;
    private JSpinner   sortSpinner;
    private JCheckBox  activeBox;

    public CategoryFormDialog(Window owner, Category category, Runnable onSuccess) {
        super(owner,
              category == null ? "Add Category" : "Edit Category — " + category.getName(),
              ModalityType.APPLICATION_MODAL);
        this.existing  = category;
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        if (existing != null) populate();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppConfig.COLOR_SURFACE);

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppConfig.COLOR_SURFACE);
        form.setBorder(new EmptyBorder(24, 28, 8, 28));

        GridBagConstraints lc = gbc(0, 0, GridBagConstraints.NONE);
        GridBagConstraints fc = gbc(1, 0, GridBagConstraints.HORIZONTAL);
        fc.weightx = 1;

        nameField = new JTextField();
        nameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Hot Beverages");
        nameField.setPreferredSize(new Dimension(300, AppConfig.FIELD_HEIGHT));

        descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(AppConfig.FONT_BODY);
        descArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Optional description…");
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(300, 80));

        sortSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        ((JSpinner.DefaultEditor) sortSpinner.getEditor()).getTextField()
                .setHorizontalAlignment(SwingConstants.LEFT);

        activeBox = new JCheckBox("Active");
        activeBox.setSelected(true);
        activeBox.setFont(AppConfig.FONT_BODY);
        activeBox.setOpaque(false);

        addRow(form, lc, fc, 0, "Name *",       nameField);
        addRow(form, lc, fc, 1, "Description",  descScroll);
        addRow(form, lc, fc, 2, "Sort Order",   sortSpinner);

        // Active checkbox row (only shown in edit mode)
        if (existing != null) {
            lc.gridy = 3; fc.gridy = 3;
            form.add(fieldLabel("Status"), lc);
            form.add(activeBox, fc);
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(AppConfig.COLOR_SURFACE);
        btnRow.setBorder(new EmptyBorder(12, 28, 20, 28));

        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.GHOST);
        cancelBtn.addActionListener(e -> dispose());

        RoundedButton saveBtn = new RoundedButton(existing == null ? "Add Category" : "Save Changes",
                RoundedButton.Style.PRIMARY);
        saveBtn.addActionListener(e -> save());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(form,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);

        // Submit on Enter in name field
        nameField.addActionListener(e -> save());
    }

    private void populate() {
        nameField.setText(existing.getName());
        descArea.setText(existing.getDescription() != null ? existing.getDescription() : "");
        sortSpinner.setValue(existing.getSortOrder());
        activeBox.setSelected(existing.isActive());
    }

    private void save() {
        try {
            String name        = nameField.getText().trim();
            String description = descArea.getText().trim();
            int    sortOrder   = (int) sortSpinner.getValue();

            if (existing == null) {
                AppContext.categoryService().create(name, description, sortOrder);
                JOptionPane.showMessageDialog(this,
                        "Category '" + name + "' created.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                existing.setName(name);
                existing.setDescription(description);
                existing.setSortOrder(sortOrder);
                existing.setActive(activeBox.isSelected());
                AppContext.categoryService().update(existing);
                JOptionPane.showMessageDialog(this,
                        "Category updated.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            onSuccess.run();
            dispose();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    private static void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc,
                                int row, String labelText, JComponent field) {
        lc.gridy = row; fc.gridy = row;
        lc.insets = new Insets(row == 0 ? 0 : 12, 0, 0, 14);
        fc.insets = new Insets(row == 0 ? 0 : 12, 0, 0, 0);
        form.add(fieldLabel(labelText), lc);
        form.add(field, fc);
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.FONT_LABEL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }

    private static GridBagConstraints gbc(int x, int y, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.fill = fill;
        c.anchor = GridBagConstraints.WEST;
        return c;
    }
}

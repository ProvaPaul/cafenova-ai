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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Modal dialog for creating or editing a {@link Product}.
 *
 * Layout: two-column split.
 *   LEFT  — 200×200 image preview + Browse button
 *   RIGHT — form fields (category combo, name, description, price, cost, checkboxes)
 *
 * Pass {@code null} for {@code product} to open in Add mode.
 */
public class ProductFormDialog extends JDialog {

    private final Product       existing;
    private final List<Category> categories;
    private final Runnable      onSuccess;

    // Left side
    private JLabel    imagePreview;
    private String    selectedImagePath;

    // Right side
    private JComboBox<Category> categoryCombo;
    private JTextField          nameField;
    private JTextArea           descArea;
    private JFormattedTextField priceField;
    private JFormattedTextField costField;
    private JCheckBox           availableBox;
    private JCheckBox           activeBox;

    public ProductFormDialog(Window owner, Product product,
                             List<Category> categories, Runnable onSuccess) {
        super(owner,
              product == null ? "Add Product" : "Edit Product — " + product.getName(),
              ModalityType.APPLICATION_MODAL);
        this.existing   = product;
        this.categories = categories;
        this.onSuccess  = onSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        if (existing != null) populate();

        pack();
        setMinimumSize(new Dimension(700, 480));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppConfig.COLOR_SURFACE);

        // ── Body: image pane + form ───────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setBackground(AppConfig.COLOR_SURFACE);
        body.setBorder(new EmptyBorder(24, 24, 12, 24));

        body.add(buildImagePane(), BorderLayout.WEST);
        body.add(buildFormPane(), BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(AppConfig.COLOR_SURFACE);
        btnRow.setBorder(new EmptyBorder(8, 24, 20, 24));

        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.GHOST);
        cancelBtn.addActionListener(e -> dispose());

        RoundedButton saveBtn = new RoundedButton(
                existing == null ? "Add Product" : "Save Changes",
                RoundedButton.Style.PRIMARY);
        saveBtn.addActionListener(e -> save());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(body,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Image pane ────────────────────────────────────────────────────────────

    private JPanel buildImagePane() {
        JPanel pane = new JPanel();
        pane.setBackground(AppConfig.COLOR_SURFACE);
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setPreferredSize(new Dimension(200, 0));

        // 200×200 placeholder box
        imagePreview = new JLabel("📷", SwingConstants.CENTER);
        imagePreview.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        imagePreview.setForeground(AppConfig.COLOR_TEXT_HINT);
        imagePreview.setPreferredSize(new Dimension(200, 200));
        imagePreview.setMaximumSize(new Dimension(200, 200));
        imagePreview.setMinimumSize(new Dimension(200, 200));
        imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreview.setVerticalAlignment(SwingConstants.CENTER);
        imagePreview.setOpaque(true);
        imagePreview.setBackground(AppConfig.COLOR_BG);
        imagePreview.setBorder(BorderFactory.createLineBorder(AppConfig.COLOR_BORDER, 1, true));

        RoundedButton browseBtn = new RoundedButton("Browse Image…", RoundedButton.Style.SECONDARY);
        browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseBtn.addActionListener(e -> browseImage());

        RoundedButton clearBtn = new RoundedButton("Remove Image", RoundedButton.Style.GHOST);
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> clearImage());

        pane.add(imagePreview);
        pane.add(Box.createVerticalStrut(10));
        pane.add(browseBtn);
        pane.add(Box.createVerticalStrut(4));
        pane.add(clearBtn);

        return pane;
    }

    private void browseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Product Image");
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif", "bmp", "webp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fc.getSelectedFile().getAbsolutePath();
            updateImagePreview(selectedImagePath);
        }
    }

    private void clearImage() {
        selectedImagePath = null;
        imagePreview.setIcon(null);
        imagePreview.setText("📷");
    }

    private void updateImagePreview(String path) {
        if (path == null || path.isBlank()) { clearImage(); return; }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image scaled   = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(scaled));
            imagePreview.setText(null);
        } catch (Exception e) {
            imagePreview.setIcon(null);
            imagePreview.setText("⚠️");
        }
    }

    // ── Form pane ─────────────────────────────────────────────────────────────

    private JPanel buildFormPane() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppConfig.COLOR_SURFACE);

        GridBagConstraints lc = gbc(0, 0, GridBagConstraints.NONE);
        GridBagConstraints fc = gbc(1, 0, GridBagConstraints.HORIZONTAL);
        fc.weightx = 1;

        // Category combo
        categoryCombo = new JComboBox<>(categories.toArray(new Category[0]));
        categoryCombo.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));

        // Name
        nameField = new JTextField();
        nameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Espresso");
        nameField.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));

        // Description
        descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(AppConfig.FONT_BODY);
        descArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Optional description…");
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 80));

        // Price (formatted)
        NumberFormat currFmt = NumberFormat.getNumberInstance(Locale.US);
        currFmt.setMinimumFractionDigits(2);
        currFmt.setMaximumFractionDigits(2);
        priceField = new JFormattedTextField(currFmt);
        priceField.setValue(0.0);
        priceField.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));

        costField = new JFormattedTextField(currFmt);
        costField.setValue(0.0);
        costField.setPreferredSize(new Dimension(0, AppConfig.FIELD_HEIGHT));

        // Checkboxes
        availableBox = new JCheckBox("Available on menu");
        availableBox.setSelected(true);
        availableBox.setFont(AppConfig.FONT_BODY);
        availableBox.setOpaque(false);

        activeBox = new JCheckBox("Active (listed in system)");
        activeBox.setSelected(true);
        activeBox.setFont(AppConfig.FONT_BODY);
        activeBox.setOpaque(false);

        addRow(form, lc, fc, 0, "Category *",     categoryCombo);
        addRow(form, lc, fc, 1, "Name *",          nameField);
        addRow(form, lc, fc, 2, "Description",     descScroll);
        addRow(form, lc, fc, 3, "Price (₱) *",     priceField);
        addRow(form, lc, fc, 4, "Cost Price (₱)",  costField);

        // Checkbox row
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        checks.setOpaque(false);
        checks.add(availableBox);
        checks.add(activeBox);
        addRow(form, lc, fc, 5, "Flags", checks);

        // Fill remaining space
        GridBagConstraints filler = gbc(0, 6, GridBagConstraints.BOTH);
        filler.gridwidth = 2;
        filler.weighty   = 1;
        form.add(new JPanel() {{ setOpaque(false); }}, filler);

        return form;
    }

    // ── Populate (edit mode) ──────────────────────────────────────────────────

    private void populate() {
        // Category
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            if (categoryCombo.getItemAt(i).getId() == existing.getCategoryId()) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }
        nameField.setText(existing.getName());
        descArea.setText(existing.getDescription() != null ? existing.getDescription() : "");
        priceField.setValue(existing.getPrice());
        costField.setValue(existing.getCostPrice());
        availableBox.setSelected(existing.isAvailable());
        activeBox.setSelected(existing.isActive());

        selectedImagePath = existing.getImagePath();
        updateImagePreview(selectedImagePath);
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void save() {
        try {
            Category selCat = (Category) categoryCombo.getSelectedItem();
            if (selCat == null) throw new com.smartcafe.exception.ValidationException("Please select a category");

            String name        = nameField.getText().trim();
            String description = descArea.getText().trim();
            double price       = ((Number) priceField.getValue()).doubleValue();
            double costPrice   = ((Number) costField.getValue()).doubleValue();
            boolean available  = availableBox.isSelected();
            boolean active     = activeBox.isSelected();

            if (existing == null) {
                AppContext.productService().create(
                        name, description, selCat.getId(),
                        price, costPrice, selectedImagePath, available);
                JOptionPane.showMessageDialog(this,
                        "Product '" + name + "' added.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                existing.setCategoryId(selCat.getId());
                existing.setName(name);
                existing.setDescription(description);
                existing.setPrice(price);
                existing.setCostPrice(costPrice);
                existing.setImagePath(selectedImagePath);
                existing.setAvailable(available);
                existing.setActive(active);
                AppContext.productService().update(existing);
                JOptionPane.showMessageDialog(this,
                        "Product updated.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            onSuccess.run();
            dispose();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc,
                                int row, String labelText, JComponent field) {
        lc.gridy = row; fc.gridy = row;
        lc.insets = new Insets(row == 0 ? 0 : 10, 0, 0, 14);
        fc.insets = new Insets(row == 0 ? 0 : 10, 0, 0, 0);
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
        c.anchor = GridBagConstraints.NORTHWEST;
        return c;
    }
}

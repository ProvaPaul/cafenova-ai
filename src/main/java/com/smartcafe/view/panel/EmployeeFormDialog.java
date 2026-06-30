package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EmployeeFormDialog extends JDialog {

    private final Employee employee;
    private final Runnable onSave;

    private final JTextField nameField   = new JTextField();
    private final JTextField phoneField  = new JTextField();
    private final JTextField emailField  = new JTextField();
    private final JTextField posField    = new JTextField();
    private final JTextField deptField   = new JTextField();
    private final JTextField salaryField = new JTextField("0.00");
    private final JTextField hireDateField = new JTextField(LocalDate.now().toString());
    private final JTextArea  addrField   = new JTextArea(2, 20);
    private final JCheckBox  activeBox   = new JCheckBox("Active", true);

    public EmployeeFormDialog(Window owner, Employee employee, Runnable onSave) {
        super(owner, employee == null ? "Add Employee" : "Edit Employee",
              ModalityType.APPLICATION_MODAL);
        this.employee = employee;
        this.onSave   = onSave;
        buildUI();
        if (employee != null) populate();
        setSize(460, 440);
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
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(3, 0, 3, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1;
        fc.insets = new Insets(3, 0, 3, 0);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        addrField.setLineWrap(true); addrField.setWrapStyleWord(true);

        int row = 0;
        addRow(form, lc, fc, row++, "Full Name *",       nameField);
        addRow(form, lc, fc, row++, "Position",          posField);
        addRow(form, lc, fc, row++, "Department",        deptField);
        addRow(form, lc, fc, row++, "Phone",             phoneField);
        addRow(form, lc, fc, row++, "Email",             emailField);
        addRow(form, lc, fc, row++, "Base Salary (₱)",   salaryField);
        addRow(form, lc, fc, row++, "Hire Date (yyyy-MM-dd)", hireDateField);

        lc.gridy = row; fc.gridy = row++;
        form.add(lbl("Address"), lc);
        fc.fill = GridBagConstraints.BOTH; fc.weighty = 0.5;
        form.add(new JScrollPane(addrField), fc);
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weighty = 0;

        lc.gridy = row; fc.gridy = row;
        form.add(new JLabel(""), lc);
        activeBox.setOpaque(false);
        activeBox.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        form.add(activeBox, fc);

        root.add(form, BorderLayout.CENTER);

        JButton save   = new JButton(employee == null ? "Add Employee" : "Save Changes");
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
        nameField.setText(employee.getFullName());
        posField.setText(employee.getPosition() != null ? employee.getPosition() : "");
        deptField.setText(employee.getDepartment() != null ? employee.getDepartment() : "");
        phoneField.setText(employee.getPhone() != null ? employee.getPhone() : "");
        emailField.setText(employee.getEmail() != null ? employee.getEmail() : "");
        salaryField.setText(String.format("%.2f", employee.getBaseSalary()));
        hireDateField.setText(employee.getHireDate() != null ? employee.getHireDate().toString() : "");
        addrField.setText(employee.getAddress() != null ? employee.getAddress() : "");
        activeBox.setSelected(employee.isActive());
    }

    private void doSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Full name is required."); return; }

        double salary;
        try { salary = Double.parseDouble(salaryField.getText().trim()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid salary amount."); return; }

        LocalDate hireDate = null;
        String hdText = hireDateField.getText().trim();
        if (!hdText.isEmpty()) {
            try { hireDate = LocalDate.parse(hdText); }
            catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Hire date must be yyyy-MM-dd."); return; }
        }

        try {
            Employee e = employee != null ? employee : new Employee();
            e.setFullName(name);
            e.setPosition(nullIfEmpty(posField.getText()));
            e.setDepartment(nullIfEmpty(deptField.getText()));
            e.setPhone(nullIfEmpty(phoneField.getText()));
            e.setEmail(nullIfEmpty(emailField.getText()));
            e.setBaseSalary(salary);
            e.setHireDate(hireDate);
            e.setAddress(nullIfEmpty(addrField.getText()));
            e.setActive(activeBox.isSelected());

            if (employee == null) AppContext.employeeService().save(e);
            else AppContext.employeeService().update(e);
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

    private static String nullIfEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}

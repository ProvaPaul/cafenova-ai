package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Attendance;
import com.smartcafe.model.Employee;
import com.smartcafe.model.SalaryPayment;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeePanel extends JPanel {

    // ── Employees tab ─────────────────────────────────────────────────────────
    private static final String[] EMP_COLS = {"ID","Name","Position","Department","Base Salary","Phone","Status"};
    private final DefaultTableModel empModel = new DefaultTableModel(EMP_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable    empTable = new JTable(empModel);
    private final JTextField empSearch = new JTextField();
    private List<Employee>  employees = new ArrayList<>();

    // ── Attendance tab ────────────────────────────────────────────────────────
    private static final String[] ATT_COLS = {"ID","Employee","Date","Status","Time In","Time Out","Notes"};
    private final DefaultTableModel attModel = new DefaultTableModel(ATT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable  attTable = new JTable(attModel);
    private final JSpinner attDateSpinner = new JSpinner(new SpinnerDateModel());
    private List<Attendance> attendances = new ArrayList<>();

    // ── Salary tab ────────────────────────────────────────────────────────────
    private static final String[] SAL_COLS = {"ID","Employee","Month","Year","Base","Bonus","Deductions","Net","Status"};
    private final DefaultTableModel salModel = new DefaultTableModel(SAL_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable  salTable = new JTable(salModel);
    private final JSpinner salMonthSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
    private final JSpinner salYearSpinner  = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2100, 1));
    private List<SalaryPayment> salaryPayments = new ArrayList<>();

    public EmployeePanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Employee Management");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage staff, attendance, and payroll.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConfig.FONT_LABEL);
        tabs.addTab("👔  Employees",  buildEmployeesTab());
        tabs.addTab("📅  Attendance", buildAttendanceTab());
        tabs.addTab("💰  Salary",     buildSalaryTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ── EMPLOYEES TAB ─────────────────────────────────────────────────────────

    private JPanel buildEmployeesTab() {
        styleTable(empTable);
        hideIdCol(empTable, empModel);
        empTable.getColumnModel().getColumn(6).setCellRenderer(activeRenderer());

        empSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search employees…");
        empSearch.setPreferredSize(new Dimension(220, 34));
        empSearch.getDocument().addDocumentListener(docListener(this::applyEmpFilter));

        RoundedButton addBtn  = new RoundedButton("+ Add",     RoundedButton.Style.PRIMARY);
        RoundedButton editBtn = new RoundedButton("✏  Edit",   RoundedButton.Style.SECONDARY);
        RoundedButton delBtn  = new RoundedButton("🗑  Delete", RoundedButton.Style.DANGER);
        editBtn.setEnabled(false); delBtn.setEnabled(false);

        empTable.getSelectionModel().addListSelectionListener(e -> {
            boolean s = empTable.getSelectedRow() >= 0;
            editBtn.setEnabled(s); delBtn.setEnabled(s);
        });
        empTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEmpEdit();
            }
        });

        addBtn.addActionListener(e  -> openEmpAdd());
        editBtn.addActionListener(e -> openEmpEdit());
        delBtn.addActionListener(e  -> deleteEmployee());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(empSearch); toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(delBtn);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(AppConfig.COLOR_BG);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(new JScrollPane(empTable), BorderLayout.CENTER);
        return p;
    }

    // ── ATTENDANCE TAB ────────────────────────────────────────────────────────

    private JPanel buildAttendanceTab() {
        styleTable(attTable);
        hideIdCol(attTable, attModel);
        attTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer());

        attDateSpinner.setEditor(new JSpinner.DateEditor(attDateSpinner, "yyyy-MM-dd"));
        attDateSpinner.setPreferredSize(new Dimension(130, 32));

        RoundedButton loadBtn = new RoundedButton("Load", RoundedButton.Style.SECONDARY);
        RoundedButton markBtn = new RoundedButton("Mark Attendance", RoundedButton.Style.PRIMARY);
        loadBtn.addActionListener(e -> loadAttendance());
        markBtn.addActionListener(e -> markAttendance());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel("Date:")); toolbar.add(attDateSpinner);
        toolbar.add(loadBtn); toolbar.add(markBtn);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(AppConfig.COLOR_BG);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(new JScrollPane(attTable), BorderLayout.CENTER);
        return p;
    }

    // ── SALARY TAB ────────────────────────────────────────────────────────────

    private JPanel buildSalaryTab() {
        styleTable(salTable);
        hideIdCol(salTable, salModel);
        salTable.getColumnModel().getColumn(8).setCellRenderer(salStatusRenderer());

        RoundedButton loadBtn     = new RoundedButton("Load",           RoundedButton.Style.SECONDARY);
        RoundedButton genAllBtn   = new RoundedButton("Generate All",   RoundedButton.Style.PRIMARY);
        RoundedButton payBtn      = new RoundedButton("Mark as Paid",   RoundedButton.Style.GHOST);
        loadBtn.addActionListener(e    -> loadSalary());
        genAllBtn.addActionListener(e  -> generateAll());
        payBtn.addActionListener(e     -> markAsPaid());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel("Month:")); toolbar.add(salMonthSpinner);
        toolbar.add(new JLabel("Year:"));  toolbar.add(salYearSpinner);
        toolbar.add(loadBtn); toolbar.add(genAllBtn); toolbar.add(payBtn);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(AppConfig.COLOR_BG);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(new JScrollPane(salTable), BorderLayout.CENTER);
        return p;
    }

    // ── PUBLIC ENTRY ─────────────────────────────────────────────────────────

    public void loadData() {
        try {
            employees = AppContext.employeeService().findAll();
        } catch (AppException ex) {
            err(ex.getMessage());
            employees = new ArrayList<>();
        }
        applyEmpFilter();
        attModel.setRowCount(0);
        salModel.setRowCount(0);
    }

    // ── EMPLOYEE ACTIONS ──────────────────────────────────────────────────────

    private void applyEmpFilter() {
        String q = empSearch.getText().trim().toLowerCase();
        empModel.setRowCount(0);
        for (Employee e : employees) {
            if (q.isEmpty() || e.getFullName().toLowerCase().contains(q)
                    || (e.getPosition() != null && e.getPosition().toLowerCase().contains(q))
                    || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(q))) {
                empModel.addRow(new Object[]{
                    e.getId(), e.getFullName(),
                    e.getPosition() != null ? e.getPosition() : "—",
                    e.getDepartment() != null ? e.getDepartment() : "—",
                    String.format("₱ %.2f", e.getBaseSalary()),
                    e.getPhone() != null ? e.getPhone() : "—",
                    e.isActive() ? "Active" : "Inactive"
                });
            }
        }
    }

    private void openEmpAdd() {
        new EmployeeFormDialog(parentWindow(), null, this::loadData).setVisible(true);
    }

    private void openEmpEdit() {
        Employee e = getSelectedEmployee();
        if (e != null) new EmployeeFormDialog(parentWindow(), e, this::loadData).setVisible(true);
    }

    private void deleteEmployee() {
        Employee e = getSelectedEmployee();
        if (e == null) return;
        if (JOptionPane.showConfirmDialog(this, "Deactivate '" + e.getFullName() + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            AppContext.employeeService().delete(e.getId());
            loadData();
        } catch (AppException ex) { err(ex.getMessage()); }
    }

    private Employee getSelectedEmployee() {
        int row = empTable.getSelectedRow();
        if (row < 0) return null;
        int id = (int) empModel.getValueAt(row, 0);
        return employees.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    // ── ATTENDANCE ACTIONS ────────────────────────────────────────────────────

    private void loadAttendance() {
        LocalDate date = toLocalDate(((SpinnerDateModel) attDateSpinner.getModel()).getDate());
        try {
            attendances = AppContext.attendanceService().getByDate(date);
        } catch (AppException ex) { err(ex.getMessage()); attendances = new ArrayList<>(); }
        attModel.setRowCount(0);
        for (Attendance a : attendances) {
            attModel.addRow(new Object[]{
                a.getId(), a.getEmployeeName(), a.getDate(),
                a.getStatus(),
                a.getTimeIn()  != null ? a.getTimeIn().toString()  : "—",
                a.getTimeOut() != null ? a.getTimeOut().toString() : "—",
                a.getNotes() != null ? a.getNotes() : ""
            });
        }
    }

    private void markAttendance() {
        if (employees.isEmpty()) {
            err("Load employee data first (go to Employees tab)."); return;
        }
        LocalDate date = toLocalDate(((SpinnerDateModel) attDateSpinner.getModel()).getDate());

        // Pick employee from dropdown
        Employee[] active = employees.stream().filter(Employee::isActive).toArray(Employee[]::new);
        if (active.length == 0) { err("No active employees."); return; }
        Employee chosen = (Employee) JOptionPane.showInputDialog(this,
                "Select employee:", "Mark Attendance", JOptionPane.PLAIN_MESSAGE,
                null, active, active[0]);
        if (chosen == null) return;

        String[] statuses = {Attendance.STATUS_PRESENT, Attendance.STATUS_ABSENT,
                             Attendance.STATUS_LATE, Attendance.STATUS_HALF_DAY, Attendance.STATUS_LEAVE};
        String status = (String) JOptionPane.showInputDialog(this,
                "Attendance status for " + chosen.getFullName() + ":",
                "Status", JOptionPane.PLAIN_MESSAGE, null, statuses, statuses[0]);
        if (status == null) return;

        Attendance att = new Attendance();
        att.setEmployeeId(chosen.getId());
        att.setDate(date);
        att.setStatus(status);

        try {
            AppContext.attendanceService().markAttendance(att);
            loadAttendance();
        } catch (AppException ex) { err(ex.getMessage()); }
    }

    // ── SALARY ACTIONS ────────────────────────────────────────────────────────

    private void loadSalary() {
        int month = (int) salMonthSpinner.getValue();
        int year  = (int) salYearSpinner.getValue();
        try {
            salaryPayments = AppContext.salaryService().getByPeriod(month, year);
        } catch (AppException ex) { err(ex.getMessage()); salaryPayments = new ArrayList<>(); }
        salModel.setRowCount(0);
        for (SalaryPayment p : salaryPayments) {
            String mName = Month.of(p.getPeriodMonth()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            salModel.addRow(new Object[]{
                p.getId(), p.getEmployeeName() != null ? p.getEmployeeName() : "—",
                mName, p.getPeriodYear(),
                String.format("₱ %.2f", p.getBaseSalary()),
                String.format("₱ %.2f", p.getBonus()),
                String.format("₱ %.2f", p.getDeductions()),
                String.format("₱ %.2f", p.getNetSalary()),
                p.getStatus()
            });
        }
    }

    private void generateAll() {
        int month = (int) salMonthSpinner.getValue();
        int year  = (int) salYearSpinner.getValue();
        try {
            List<Employee> active = AppContext.employeeService().findAllActive();
            AppContext.salaryService().generateAll(active, month, year);
            loadSalary();
            JOptionPane.showMessageDialog(this, "Payroll generated for " + active.size() + " employees.");
        } catch (AppException ex) { err(ex.getMessage()); }
    }

    private void markAsPaid() {
        int row = salTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a salary record."); return; }
        int id = (int) salModel.getValueAt(row, 0);
        try {
            AppContext.salaryService().markAsPaid(id);
            loadSalary();
        } catch (AppException ex) { err(ex.getMessage()); }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private static void styleTable(JTable t) {
        t.setFont(AppConfig.FONT_BODY);
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(AppConfig.COLOR_BORDER);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(AppConfig.FONT_LABEL);
    }

    private static void hideIdCol(JTable t, DefaultTableModel m) {
        TableColumn col = t.getColumnModel().getColumn(0);
        col.setMaxWidth(0); col.setMinWidth(0); col.setPreferredWidth(0);
    }

    private static LocalDate toLocalDate(java.util.Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private Window parentWindow() { return SwingUtilities.getWindowAncestor(this); }
    private void err(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    private static javax.swing.event.DocumentListener docListener(Runnable r) {
        return new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        };
    }

    private static DefaultTableCellRenderer activeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                setForeground("Active".equals(v) ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_TEXT_HINT);
                setHorizontalAlignment(CENTER); return this;
            }
        };
    }

    private static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                String s = v != null ? v.toString() : "";
                Color col = switch (s) {
                    case Attendance.STATUS_PRESENT  -> AppConfig.COLOR_SUCCESS;
                    case Attendance.STATUS_ABSENT   -> AppConfig.COLOR_ERROR;
                    case Attendance.STATUS_LATE     -> AppConfig.COLOR_WARNING;
                    case Attendance.STATUS_HALF_DAY -> AppConfig.COLOR_INFO;
                    default -> AppConfig.COLOR_TEXT_SECONDARY;
                };
                setForeground(col); setHorizontalAlignment(CENTER); return this;
            }
        };
    }

    private static DefaultTableCellRenderer salStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                setForeground(SalaryPayment.STATUS_PAID.equals(v) ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_WARNING);
                setHorizontalAlignment(CENTER); return this;
            }
        };
    }
}

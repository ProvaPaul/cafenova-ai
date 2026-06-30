package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Reservation;
import com.smartcafe.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationPanel extends JPanel {

    private static final String[] COLS =
        {"ID", "Customer", "Table", "Date", "Time", "Party", "Status", "Notes"};

    private final DefaultTableModel model;
    private final JTable            table;
    private final JSpinner          fromSpinner, toSpinner;
    private final JComboBox<String> statusCombo;
    private final RoundedButton     editBtn, confirmBtn, cancelBtn, completeBtn;
    private List<Reservation>       reservations = new ArrayList<>();

    public ReservationPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Table Reservations");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage dine-in reservations and table availability.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        // Filter bar
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();
        fromSpinner = new JSpinner(fromModel);
        toSpinner   = new JSpinner(toModel);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner,   "yyyy-MM-dd"));
        fromSpinner.setPreferredSize(new Dimension(130, 32));
        toSpinner.setPreferredSize(new Dimension(130, 32));

        java.util.Calendar cal = java.util.Calendar.getInstance();
        toModel.setValue(cal.getTime());
        cal.add(java.util.Calendar.DAY_OF_MONTH, -7);
        fromModel.setValue(cal.getTime());

        statusCombo = new JComboBox<>(new String[]{"ALL",
            Reservation.STATUS_PENDING, Reservation.STATUS_CONFIRMED,
            Reservation.STATUS_COMPLETED, Reservation.STATUS_CANCELLED, Reservation.STATUS_NO_SHOW});
        statusCombo.setPreferredSize(new Dimension(130, 32));

        RoundedButton searchBtn = new RoundedButton("🔍  Search", RoundedButton.Style.SECONDARY);
        RoundedButton addBtn    = new RoundedButton("+ Add",      RoundedButton.Style.PRIMARY);
        searchBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e    -> openAdd());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);
        filterBar.add(lbl("From:")); filterBar.add(fromSpinner);
        filterBar.add(lbl("To:"));   filterBar.add(toSpinner);
        filterBar.add(lbl("Status:")); filterBar.add(statusCombo);
        filterBar.add(searchBtn);

        editBtn     = new RoundedButton("✏  Edit",     RoundedButton.Style.SECONDARY);
        confirmBtn  = new RoundedButton("✔  Confirm",  RoundedButton.Style.PRIMARY);
        cancelBtn   = new RoundedButton("✖  Cancel",   RoundedButton.Style.DANGER);
        completeBtn = new RoundedButton("✓  Complete", RoundedButton.Style.GHOST);
        disableActionBtns();

        editBtn.addActionListener(e     -> openEdit());
        confirmBtn.addActionListener(e  -> updateStatus(Reservation.STATUS_CONFIRMED));
        cancelBtn.addActionListener(e   -> updateStatus(Reservation.STATUS_CANCELLED));
        completeBtn.addActionListener(e -> updateStatus(Reservation.STATUS_COMPLETED));

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBar.setOpaque(false);
        actionBar.add(completeBtn); actionBar.add(editBtn);
        actionBar.add(confirmBtn); actionBar.add(cancelBtn); actionBar.add(addBtn);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        toolbar.add(filterBar,  BorderLayout.WEST);
        toolbar.add(actionBar, BorderLayout.EAST);

        // Table
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(AppConfig.FONT_BODY);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.COLOR_BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(AppConfig.FONT_LABEL);

        // Hide ID col
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMaxWidth(0); idCol.setMinWidth(0); idCol.setPreferredWidth(0);

        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());

        table.getSelectionModel().addListSelectionListener(e -> {
            Reservation r = getSelected();
            boolean sel = r != null;
            editBtn.setEnabled(sel);
            confirmBtn.setEnabled(sel && Reservation.STATUS_PENDING.equals(r.getStatus()));
            cancelBtn.setEnabled(sel && !Reservation.STATUS_CANCELLED.equals(r.getStatus())
                                     && !Reservation.STATUS_COMPLETED.equals(r.getStatus()));
            completeBtn.setEnabled(sel && Reservation.STATUS_CONFIRMED.equals(r.getStatus()));
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEdit();
            }
        });

        JLabel countLbl = new JLabel(" ");
        countLbl.setFont(AppConfig.FONT_SMALL);
        countLbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        model.addTableModelListener(ev -> countLbl.setText(model.getRowCount() + " reservation(s)"));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolbar,               BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(countLbl,              BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        LocalDate from = toLocalDate(((SpinnerDateModel) fromSpinner.getModel()).getDate());
        LocalDate to   = toLocalDate(((SpinnerDateModel) toSpinner.getModel()).getDate());
        String status  = (String) statusCombo.getSelectedItem();
        try {
            reservations = AppContext.reservationService().findByDateRange(from, to, status);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            reservations = new ArrayList<>();
        }
        rebuildTable();
    }

    private void rebuildTable() {
        model.setRowCount(0);
        for (Reservation r : reservations) {
            model.addRow(new Object[]{
                r.getId(),
                r.getCustomerName(),
                r.getTableNumber() != null ? r.getTableNumber() : "—",
                r.getReservationDate(),
                r.getReservationTime(),
                r.getPartySize(),
                r.getStatus(),
                r.getNotes() != null ? r.getNotes() : ""
            });
        }
        disableActionBtns();
    }

    private void openAdd() {
        new ReservationFormDialog(parentWindow(), null, this::loadData).setVisible(true);
    }

    private void openEdit() {
        Reservation r = getSelected();
        if (r != null) new ReservationFormDialog(parentWindow(), r, this::loadData).setVisible(true);
    }

    private void updateStatus(String newStatus) {
        Reservation r = getSelected();
        if (r == null) return;
        try {
            AppContext.reservationService().updateStatus(r.getId(), newStatus);
            loadData();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Reservation getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return reservations.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    private void disableActionBtns() {
        editBtn.setEnabled(false); confirmBtn.setEnabled(false);
        cancelBtn.setEnabled(false); completeBtn.setEnabled(false);
    }

    private Window parentWindow() { return SwingUtilities.getWindowAncestor(this); }

    private static LocalDate toLocalDate(java.util.Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private static JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppConfig.FONT_SMALL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }

    private static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                Color col = switch (v != null ? v.toString() : "") {
                    case Reservation.STATUS_CONFIRMED -> AppConfig.COLOR_SUCCESS;
                    case Reservation.STATUS_CANCELLED,
                         Reservation.STATUS_NO_SHOW  -> AppConfig.COLOR_ERROR;
                    case Reservation.STATUS_COMPLETED -> AppConfig.COLOR_INFO;
                    default                           -> AppConfig.COLOR_WARNING;
                };
                setForeground(col); setHorizontalAlignment(CENTER); return this;
            }
        };
    }
}

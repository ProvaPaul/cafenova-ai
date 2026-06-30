package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.CafeTable;
import com.smartcafe.model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReservationFormDialog extends JDialog {

    private final Reservation reservation;
    private final Runnable    onSave;

    private final JTextField  nameField   = new JTextField();
    private final JTextField  dateField   = new JTextField(LocalDate.now().toString());
    private final JTextField  timeField   = new JTextField("12:00");
    private final JSpinner    partySpinner= new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
    private final JTextArea   notesField  = new JTextArea(3, 20);
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{
        Reservation.STATUS_PENDING, Reservation.STATUS_CONFIRMED,
        Reservation.STATUS_COMPLETED, Reservation.STATUS_CANCELLED, Reservation.STATUS_NO_SHOW});
    private JComboBox<Object> tableCombo;

    public ReservationFormDialog(Window owner, Reservation reservation, Runnable onSave) {
        super(owner, reservation == null ? "New Reservation" : "Edit Reservation",
              ModalityType.APPLICATION_MODAL);
        this.reservation = reservation;
        this.onSave      = onSave;
        buildUI();
        if (reservation != null) populate();
        setSize(440, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        // Build table combo from available tables
        tableCombo = new JComboBox<>();
        tableCombo.addItem("— No table —");
        try (var c = DatabaseConfig.getConnection();
             var ps = c.prepareStatement("SELECT id, table_number, capacity, location FROM cafe_tables ORDER BY table_number");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                CafeTable t = new CafeTable();
                t.setId(rs.getInt("id"));
                t.setTableNumber(rs.getString("table_number"));
                t.setCapacity(rs.getInt("capacity"));
                t.setLocation(rs.getString("location"));
                tableCombo.addItem(t);
            }
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        root.setBackground(AppConfig.COLOR_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(4, 0, 4, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1;
        fc.insets = new Insets(4, 0, 4, 0);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        notesField.setLineWrap(true); notesField.setWrapStyleWord(true);

        int row = 0;
        addRow(form, lc, fc, row++, "Customer Name *", nameField);
        addRow(form, lc, fc, row++, "Date (yyyy-MM-dd)", dateField);
        addRow(form, lc, fc, row++, "Time (HH:mm)",      timeField);
        addRow(form, lc, fc, row++, "Party Size",        partySpinner);
        addRow(form, lc, fc, row++, "Table",             tableCombo);
        addRow(form, lc, fc, row++, "Status",            statusCombo);

        lc.gridy = row; fc.gridy = row;
        form.add(lbl("Notes"), lc);
        fc.fill = GridBagConstraints.BOTH; fc.weighty = 1;
        form.add(new JScrollPane(notesField), fc);

        root.add(form, BorderLayout.CENTER);

        JButton save   = new JButton(reservation == null ? "Add Reservation" : "Save Changes");
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
        nameField.setText(reservation.getCustomerName());
        dateField.setText(reservation.getReservationDate() != null ? reservation.getReservationDate().toString() : "");
        timeField.setText(reservation.getReservationTime() != null ? reservation.getReservationTime().toString() : "");
        partySpinner.setValue(reservation.getPartySize());
        statusCombo.setSelectedItem(reservation.getStatus());
        if (reservation.getNotes() != null) notesField.setText(reservation.getNotes());
    }

    private void doSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Customer name is required."); return; }

        LocalDate date;
        LocalTime time;
        try { date = LocalDate.parse(dateField.getText().trim()); }
        catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Invalid date (yyyy-MM-dd)."); return; }
        try { time = LocalTime.parse(timeField.getText().trim()); }
        catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Invalid time (HH:mm)."); return; }

        Reservation r = reservation != null ? reservation : new Reservation();
        r.setCustomerName(name);
        r.setReservationDate(date);
        r.setReservationTime(time);
        r.setPartySize((int) partySpinner.getValue());
        r.setStatus((String) statusCombo.getSelectedItem());
        r.setNotes(notesField.getText().trim().isEmpty() ? null : notesField.getText().trim());

        Object sel = tableCombo.getSelectedItem();
        if (sel instanceof CafeTable ct) r.setTableId(ct.getId());
        else r.setTableId(null);

        try {
            if (reservation == null) AppContext.reservationService().save(r);
            else AppContext.reservationService().update(r);
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
}

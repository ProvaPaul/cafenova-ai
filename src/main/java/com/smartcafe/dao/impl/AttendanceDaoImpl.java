package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.AttendanceDao;
import com.smartcafe.model.Attendance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceDaoImpl implements AttendanceDao {

    private static final String BASE =
        "SELECT a.*, e.full_name AS employee_name FROM attendance a " +
        "JOIN employees e ON a.employee_id = e.id ";

    @Override
    public List<Attendance> findByDate(LocalDate date) throws SQLException {
        String sql = BASE + "WHERE a.date = ? ORDER BY e.full_name";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            return list(ps);
        }
    }

    @Override
    public List<Attendance> findByEmployeeAndPeriod(int employeeId, LocalDate from, LocalDate to) throws SQLException {
        String sql = BASE + "WHERE a.employee_id = ? AND a.date BETWEEN ? AND ? ORDER BY a.date";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            return list(ps);
        }
    }

    @Override
    public Optional<Attendance> findByEmployeeAndDate(int employeeId, LocalDate date) throws SQLException {
        String sql = BASE + "WHERE a.employee_id = ? AND a.date = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Attendance save(Attendance a) throws SQLException {
        String sql = "INSERT INTO attendance (employee_id, date, time_in, time_out, status, notes) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, a);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setId(keys.getInt(1));
            }
            return a;
        }
    }

    @Override
    public void update(Attendance a) throws SQLException {
        String sql = "UPDATE attendance SET employee_id=?, date=?, time_in=?, time_out=?, status=?, notes=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, a);
            ps.setInt(7, a.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Attendance a) throws SQLException {
        ps.setInt(1, a.getEmployeeId());
        ps.setDate(2, Date.valueOf(a.getDate()));
        ps.setObject(3, a.getTimeIn());
        ps.setObject(4, a.getTimeOut());
        ps.setString(5, a.getStatus());
        ps.setString(6, a.getNotes());
    }

    private List<Attendance> list(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Attendance> lst = new ArrayList<>();
            while (rs.next()) lst.add(map(rs));
            return lst;
        }
    }

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setEmployeeId(rs.getInt("employee_id"));
        Date d = rs.getDate("date");
        if (d != null) a.setDate(d.toLocalDate());
        Time ti = rs.getTime("time_in");
        if (ti != null) a.setTimeIn(ti.toLocalTime());
        Time to = rs.getTime("time_out");
        if (to != null) a.setTimeOut(to.toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        try { a.setEmployeeName(rs.getString("employee_name")); } catch (SQLException ignored) {}
        return a;
    }
}

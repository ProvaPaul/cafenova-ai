package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.SalaryPaymentDao;
import com.smartcafe.model.SalaryPayment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalaryPaymentDaoImpl implements SalaryPaymentDao {

    private static final String BASE =
        "SELECT sp.*, e.full_name AS employee_name FROM salary_payments sp " +
        "JOIN employees e ON sp.employee_id = e.id ";

    @Override
    public List<SalaryPayment> findByPeriod(int month, int year) throws SQLException {
        String sql = BASE + "WHERE sp.period_month = ? AND sp.period_year = ? ORDER BY e.full_name";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, month); ps.setInt(2, year);
            return list(ps);
        }
    }

    @Override
    public List<SalaryPayment> findByEmployee(int employeeId) throws SQLException {
        String sql = BASE + "WHERE sp.employee_id = ? ORDER BY sp.period_year DESC, sp.period_month DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return list(ps);
        }
    }

    @Override
    public Optional<SalaryPayment> findByEmployeeAndPeriod(int employeeId, int month, int year) throws SQLException {
        String sql = BASE + "WHERE sp.employee_id = ? AND sp.period_month = ? AND sp.period_year = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId); ps.setInt(2, month); ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public SalaryPayment save(SalaryPayment p) throws SQLException {
        String sql = "INSERT INTO salary_payments (employee_id, period_month, period_year, base_salary, bonus, deductions, net_salary, status, notes) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p;
        }
    }

    @Override
    public void update(SalaryPayment p) throws SQLException {
        String sql = "UPDATE salary_payments SET employee_id=?, period_month=?, period_year=?, base_salary=?, bonus=?, deductions=?, net_salary=?, status=?, notes=?, paid_at=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setObject(10, p.getPaidAt() != null ? Timestamp.valueOf(p.getPaidAt()) : null);
            ps.setInt(11, p.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, SalaryPayment p) throws SQLException {
        ps.setInt(1, p.getEmployeeId());
        ps.setInt(2, p.getPeriodMonth());
        ps.setInt(3, p.getPeriodYear());
        ps.setDouble(4, p.getBaseSalary());
        ps.setDouble(5, p.getBonus());
        ps.setDouble(6, p.getDeductions());
        ps.setDouble(7, p.getNetSalary());
        ps.setString(8, p.getStatus());
        ps.setString(9, p.getNotes());
    }

    private List<SalaryPayment> list(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<SalaryPayment> lst = new ArrayList<>();
            while (rs.next()) lst.add(map(rs));
            return lst;
        }
    }

    private SalaryPayment map(ResultSet rs) throws SQLException {
        SalaryPayment p = new SalaryPayment();
        p.setId(rs.getInt("id"));
        p.setEmployeeId(rs.getInt("employee_id"));
        p.setPeriodMonth(rs.getInt("period_month"));
        p.setPeriodYear(rs.getInt("period_year"));
        p.setBaseSalary(rs.getDouble("base_salary"));
        p.setBonus(rs.getDouble("bonus"));
        p.setDeductions(rs.getDouble("deductions"));
        p.setNetSalary(rs.getDouble("net_salary"));
        p.setStatus(rs.getString("status"));
        p.setNotes(rs.getString("notes"));
        Timestamp pa = rs.getTimestamp("paid_at");
        if (pa != null) p.setPaidAt(pa.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        try { p.setEmployeeName(rs.getString("employee_name")); } catch (SQLException ignored) {}
        return p;
    }
}

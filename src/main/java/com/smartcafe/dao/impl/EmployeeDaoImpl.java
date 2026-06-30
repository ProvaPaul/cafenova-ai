package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.EmployeeDao;
import com.smartcafe.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDaoImpl implements EmployeeDao {

    @Override
    public List<Employee> findAll() throws SQLException {
        return query("SELECT * FROM employees ORDER BY full_name", ps -> {});
    }

    @Override
    public List<Employee> findAllActive() throws SQLException {
        return query("SELECT * FROM employees WHERE is_active = TRUE ORDER BY full_name", ps -> {});
    }

    @Override
    public Optional<Employee> findById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Employee save(Employee emp) throws SQLException {
        String sql = "INSERT INTO employees (full_name, phone, email, address, position, department, base_salary, hire_date, is_active, user_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, emp);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) emp.setId(keys.getInt(1));
            }
            return emp;
        }
    }

    @Override
    public void update(Employee emp) throws SQLException {
        String sql = "UPDATE employees SET full_name=?, phone=?, email=?, address=?, position=?, department=?, base_salary=?, hire_date=?, is_active=?, user_id=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, emp);
            ps.setInt(11, emp.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "UPDATE employees SET is_active = FALSE WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Employee emp) throws SQLException {
        ps.setString(1, emp.getFullName());
        ps.setString(2, emp.getPhone());
        ps.setString(3, emp.getEmail());
        ps.setString(4, emp.getAddress());
        ps.setString(5, emp.getPosition());
        ps.setString(6, emp.getDepartment());
        ps.setDouble(7, emp.getBaseSalary());
        ps.setObject(8, emp.getHireDate());
        ps.setBoolean(9, emp.isActive());
        if (emp.getUserId() != null) ps.setInt(10, emp.getUserId());
        else ps.setNull(10, Types.INTEGER);
    }

    private List<Employee> query(String sql, SqlConsumer<PreparedStatement> binder) throws SQLException {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Employee> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setFullName(rs.getString("full_name"));
        e.setPhone(rs.getString("phone"));
        e.setEmail(rs.getString("email"));
        e.setAddress(rs.getString("address"));
        e.setPosition(rs.getString("position"));
        e.setDepartment(rs.getString("department"));
        e.setBaseSalary(rs.getDouble("base_salary"));
        Date hd = rs.getDate("hire_date");
        if (hd != null) e.setHireDate(hd.toLocalDate());
        e.setActive(rs.getBoolean("is_active"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) e.setUserId(uid);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        return e;
    }

    @FunctionalInterface
    interface SqlConsumer<T> { void accept(T t) throws SQLException; }
}

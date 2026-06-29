package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.SupplierDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDaoImpl implements SupplierDao {

    @Override
    public List<Supplier> findAll() {
        final String sql = "SELECT * FROM suppliers ORDER BY name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Supplier> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.findAll", e); }
    }

    @Override
    public List<Supplier> findAllActive() {
        final String sql = "SELECT * FROM suppliers WHERE is_active = TRUE ORDER BY name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Supplier> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.findAllActive", e); }
    }

    @Override
    public Optional<Supplier> findById(int id) {
        final String sql = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.findById", e); }
    }

    @Override
    public boolean existsByName(String name) {
        final String sql = "SELECT 1 FROM suppliers WHERE LOWER(name)=LOWER(?) AND is_active=TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.existsByName", e); }
    }

    @Override
    public boolean existsByNameExcludingId(String name, int excludeId) {
        final String sql =
            "SELECT 1 FROM suppliers WHERE LOWER(name)=LOWER(?) AND id!=? AND is_active=TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.existsByNameExcludingId", e); }
    }

    @Override
    public Supplier save(Supplier s) {
        final String sql =
            "INSERT INTO suppliers (name, contact, phone, email, address, is_active) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContact());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setBoolean(6, s.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }
            return s;
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.save", e); }
    }

    @Override
    public void update(Supplier s) {
        final String sql =
            "UPDATE suppliers SET name=?, contact=?, phone=?, email=?, address=?, is_active=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContact());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setBoolean(6, s.isActive());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.update", e); }
    }

    @Override
    public void delete(int id) {
        final String sql = "UPDATE suppliers SET is_active = FALSE WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.delete", e); }
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM suppliers WHERE is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) { throw new DatabaseException("SupplierDao.count", e); }
    }

    private Supplier map(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setContact(rs.getString("contact"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }
}

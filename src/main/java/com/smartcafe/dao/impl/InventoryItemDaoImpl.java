package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.InventoryItemDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.InventoryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryItemDaoImpl implements InventoryItemDao {

    private static final String BASE_SELECT =
        "SELECT i.*, s.name AS supplier_name " +
        "FROM inventory_items i " +
        "LEFT JOIN suppliers s ON i.supplier_id = s.id ";

    @Override
    public List<InventoryItem> findAll() {
        final String sql = BASE_SELECT +
            "WHERE i.is_active = TRUE ORDER BY i.name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<InventoryItem> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.findAll", e); }
    }

    @Override
    public List<InventoryItem> findLowStock() {
        final String sql = BASE_SELECT +
            "WHERE i.is_active = TRUE AND i.current_stock <= i.min_stock ORDER BY i.name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<InventoryItem> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.findLowStock", e); }
    }

    @Override
    public Optional<InventoryItem> findById(int id) {
        final String sql = BASE_SELECT + "WHERE i.id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.findById", e); }
    }

    @Override
    public boolean existsByName(String name) {
        final String sql = "SELECT 1 FROM inventory_items WHERE LOWER(name)=LOWER(?) AND is_active=TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.existsByName", e); }
    }

    @Override
    public boolean existsByNameExcludingId(String name, int excludeId) {
        final String sql =
            "SELECT 1 FROM inventory_items WHERE LOWER(name)=LOWER(?) AND id!=? AND is_active=TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.existsByNameExcludingId", e); }
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        final String sql =
            "INSERT INTO inventory_items (name, unit, current_stock, min_stock, cost_per_unit, supplier_id, is_active)" +
            " VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setDouble(3, item.getCurrentStock());
            ps.setDouble(4, item.getMinStock());
            if (item.getCostPerUnit() != null) ps.setDouble(5, item.getCostPerUnit());
            else ps.setNull(5, Types.DECIMAL);
            if (item.getSupplierId() != null) ps.setInt(6, item.getSupplierId());
            else ps.setNull(6, Types.INTEGER);
            ps.setBoolean(7, item.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }
            return item;
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.save", e); }
    }

    @Override
    public void update(InventoryItem item) {
        final String sql =
            "UPDATE inventory_items SET name=?, unit=?, current_stock=?, min_stock=?," +
            " cost_per_unit=?, supplier_id=?, is_active=?, updated_at=NOW() WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setDouble(3, item.getCurrentStock());
            ps.setDouble(4, item.getMinStock());
            if (item.getCostPerUnit() != null) ps.setDouble(5, item.getCostPerUnit());
            else ps.setNull(5, Types.DECIMAL);
            if (item.getSupplierId() != null) ps.setInt(6, item.getSupplierId());
            else ps.setNull(6, Types.INTEGER);
            ps.setBoolean(7, item.isActive());
            ps.setInt(8, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.update", e); }
    }

    @Override
    public void delete(int id) {
        final String sql = "UPDATE inventory_items SET is_active = FALSE, updated_at = NOW() WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.delete", e); }
    }

    @Override
    public long countLowStock() {
        final String sql =
            "SELECT COUNT(*) FROM inventory_items WHERE is_active=TRUE AND current_stock <= min_stock";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) { throw new DatabaseException("InventoryDao.countLowStock", e); }
    }

    private InventoryItem map(ResultSet rs) throws SQLException {
        InventoryItem i = new InventoryItem();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setUnit(rs.getString("unit"));
        i.setCurrentStock(rs.getDouble("current_stock"));
        i.setMinStock(rs.getDouble("min_stock"));
        double cpu = rs.getDouble("cost_per_unit");
        if (!rs.wasNull()) i.setCostPerUnit(cpu);
        int sid = rs.getInt("supplier_id");
        if (!rs.wasNull()) i.setSupplierId(sid);
        i.setActive(rs.getBoolean("is_active"));
        i.setSupplierName(rs.getString("supplier_name"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) i.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) i.setUpdatedAt(ua.toLocalDateTime());
        return i;
    }
}

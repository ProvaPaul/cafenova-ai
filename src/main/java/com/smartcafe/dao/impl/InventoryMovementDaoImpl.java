package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.InventoryMovementDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.InventoryMovement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryMovementDaoImpl implements InventoryMovementDao {

    @Override
    public void record(InventoryMovement m) {
        final String sql =
            "INSERT INTO inventory_movements " +
            "(inventory_id, movement_type, quantity, quantity_before, quantity_after, reference_id, notes, created_by) " +
            "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, m.getInventoryId());
            ps.setString(2, m.getMovementType());
            ps.setDouble(3, m.getQuantity());
            ps.setDouble(4, m.getQuantityBefore());
            ps.setDouble(5, m.getQuantityAfter());
            if (m.getReferenceId() != null) ps.setInt(6, m.getReferenceId());
            else ps.setNull(6, Types.INTEGER);
            ps.setString(7, m.getNotes());
            if (m.getCreatedBy() != null) ps.setInt(8, m.getCreatedBy());
            else ps.setNull(8, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("InventoryMovementDao.record", e); }
    }

    @Override
    public List<InventoryMovement> findByInventoryId(int inventoryId) {
        final String sql =
            "SELECT im.*, ii.name AS inv_name, u.full_name AS user_name " +
            "FROM inventory_movements im " +
            "JOIN inventory_items ii ON im.inventory_id = ii.id " +
            "LEFT JOIN users u ON im.created_by = u.id " +
            "WHERE im.inventory_id = ? ORDER BY im.created_at DESC LIMIT 200";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            return map(ps.executeQuery());
        } catch (SQLException e) { throw new DatabaseException("InventoryMovementDao.findByInventoryId", e); }
    }

    @Override
    public List<InventoryMovement> findRecent(int limit) {
        final String sql =
            "SELECT im.*, ii.name AS inv_name, u.full_name AS user_name " +
            "FROM inventory_movements im " +
            "JOIN inventory_items ii ON im.inventory_id = ii.id " +
            "LEFT JOIN users u ON im.created_by = u.id " +
            "ORDER BY im.created_at DESC LIMIT ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            return map(ps.executeQuery());
        } catch (SQLException e) { throw new DatabaseException("InventoryMovementDao.findRecent", e); }
    }

    private List<InventoryMovement> map(ResultSet rs) throws SQLException {
        List<InventoryMovement> list = new ArrayList<>();
        while (rs.next()) {
            InventoryMovement m = new InventoryMovement();
            m.setId(rs.getInt("id"));
            m.setInventoryId(rs.getInt("inventory_id"));
            m.setInventoryName(rs.getString("inv_name"));
            m.setMovementType(rs.getString("movement_type"));
            m.setQuantity(rs.getDouble("quantity"));
            m.setQuantityBefore(rs.getDouble("quantity_before"));
            m.setQuantityAfter(rs.getDouble("quantity_after"));
            int ref = rs.getInt("reference_id");
            if (!rs.wasNull()) m.setReferenceId(ref);
            m.setNotes(rs.getString("notes"));
            int cb = rs.getInt("created_by");
            if (!rs.wasNull()) m.setCreatedBy(cb);
            m.setCreatedByName(rs.getString("user_name"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
            list.add(m);
        }
        return list;
    }
}

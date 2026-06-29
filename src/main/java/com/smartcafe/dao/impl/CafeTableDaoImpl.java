package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.CafeTableDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.CafeTable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CafeTableDaoImpl implements CafeTableDao {

    @Override
    public List<CafeTable> findAll() {
        final String sql = "SELECT * FROM cafe_tables ORDER BY table_number ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CafeTable> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new DatabaseException("CafeTableDao.findAll", e); }
    }

    @Override
    public List<CafeTable> findAvailable() {
        final String sql =
            "SELECT * FROM cafe_tables WHERE status = 'AVAILABLE' ORDER BY table_number ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CafeTable> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new DatabaseException("CafeTableDao.findAvailable", e); }
    }

    @Override
    public Optional<CafeTable> findById(int id) {
        final String sql = "SELECT * FROM cafe_tables WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("CafeTableDao.findById", e); }
    }

    @Override
    public void updateStatus(Connection conn, int tableId, String status) throws SQLException {
        final String sql = "UPDATE cafe_tables SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, tableId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateStatus(int tableId, String status) {
        final String sql = "UPDATE cafe_tables SET status = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, tableId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("CafeTableDao.updateStatus", e); }
    }

    private CafeTable map(ResultSet rs) throws SQLException {
        CafeTable t = new CafeTable();
        t.setId(rs.getInt("id"));
        t.setTableNumber(rs.getString("table_number"));
        t.setCapacity(rs.getInt("capacity"));
        t.setLocation(rs.getString("location"));
        t.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }
}

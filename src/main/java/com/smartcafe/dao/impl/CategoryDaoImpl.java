package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.CategoryDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl implements CategoryDao {

    @Override
    public List<Category> findAll() {
        final String sql =
            "SELECT * FROM categories WHERE is_active = TRUE ORDER BY sort_order ASC, name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Category> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.findAll failed", e);
        }
    }

    @Override
    public List<Category> search(String query) {
        final String sql =
            "SELECT * FROM categories WHERE is_active = TRUE " +
            "AND (name LIKE ? OR description LIKE ?) " +
            "ORDER BY sort_order ASC, name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + query + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Category> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.search failed", e);
        }
    }

    @Override
    public Optional<Category> findById(int id) {
        final String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.findById failed", e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        final String sql = "SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?) AND is_active = TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.existsByName failed", e);
        }
    }

    @Override
    public boolean existsByNameExcludingId(String name, int excludeId) {
        final String sql =
            "SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?) AND id != ? AND is_active = TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.existsByNameExcludingId failed", e);
        }
    }

    @Override
    public Category save(Category category) {
        final String sql =
            "INSERT INTO categories (name, description, sort_order, is_active) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getSortOrder());
            ps.setBoolean(4, category.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) category.setId(keys.getInt(1));
            }
            return category;
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.save failed", e);
        }
    }

    @Override
    public void update(Category category) {
        final String sql =
            "UPDATE categories SET name=?, description=?, sort_order=?, is_active=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getSortOrder());
            ps.setBoolean(4, category.isActive());
            ps.setInt(5, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.update failed", e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "UPDATE categories SET is_active = FALSE WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.delete failed", e);
        }
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM categories WHERE is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DatabaseException("CategoryDao.count failed", e);
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setId(rs.getInt("id"));
        cat.setName(rs.getString("name"));
        cat.setDescription(rs.getString("description"));
        cat.setSortOrder(rs.getInt("sort_order"));
        cat.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) cat.setCreatedAt(ts.toLocalDateTime());
        return cat;
    }
}

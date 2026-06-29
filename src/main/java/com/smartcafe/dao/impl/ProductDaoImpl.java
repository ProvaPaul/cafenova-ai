package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.ProductDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {

    // Base SELECT that always JOINs the category name
    private static final String BASE_SELECT =
        "SELECT m.*, c.name AS category_name FROM menu_items m " +
        "LEFT JOIN categories c ON m.category_id = c.id ";

    @Override
    public List<Product> findAll() {
        final String sql = BASE_SELECT +
            "WHERE m.is_active = TRUE ORDER BY c.sort_order ASC, m.name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.findAll failed", e);
        }
    }

    @Override
    public List<Product> findByCategory(int categoryId) {
        final String sql = BASE_SELECT +
            "WHERE m.category_id = ? AND m.is_active = TRUE ORDER BY m.name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.findByCategory failed", e);
        }
    }

    @Override
    public List<Product> search(String query) {
        final String sql = BASE_SELECT +
            "WHERE m.is_active = TRUE AND (m.name LIKE ? OR m.description LIKE ?) " +
            "ORDER BY c.sort_order ASC, m.name ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + query + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.search failed", e);
        }
    }

    @Override
    public List<Product> searchInCategory(String query, Integer categoryId) {
        StringBuilder sb = new StringBuilder(BASE_SELECT).append("WHERE m.is_active = TRUE ");
        if (categoryId != null) sb.append("AND m.category_id = ? ");
        if (query != null && !query.isBlank()) sb.append("AND (m.name LIKE ? OR m.description LIKE ?) ");
        sb.append("ORDER BY c.sort_order ASC, m.name ASC");

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int idx = 1;
            if (categoryId != null) ps.setInt(idx++, categoryId);
            if (query != null && !query.isBlank()) {
                String like = "%" + query + "%";
                ps.setString(idx++, like);
                ps.setString(idx, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.searchInCategory failed", e);
        }
    }

    @Override
    public Optional<Product> findById(int id) {
        final String sql = BASE_SELECT + "WHERE m.id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.findById failed", e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        final String sql = "SELECT 1 FROM menu_items WHERE LOWER(name)=LOWER(?) AND is_active=TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.existsByName failed", e);
        }
    }

    @Override
    public boolean existsByNameExcludingId(String name, int excludeId) {
        final String sql =
            "SELECT 1 FROM menu_items WHERE LOWER(name)=LOWER(?) AND id!=? AND is_active=TRUE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.existsByNameExcludingId failed", e);
        }
    }

    @Override
    public Product save(Product product) {
        final String sql =
            "INSERT INTO menu_items " +
            "(category_id, name, description, price, cost_price, image_path, is_available, is_active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setDouble(5, product.getCostPrice());
            ps.setString(6, product.getImagePath());
            ps.setBoolean(7, product.isAvailable());
            ps.setBoolean(8, product.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) product.setId(keys.getInt(1));
            }
            return product;
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.save failed", e);
        }
    }

    @Override
    public void update(Product product) {
        final String sql =
            "UPDATE menu_items SET category_id=?, name=?, description=?, price=?, " +
            "cost_price=?, image_path=?, is_available=?, is_active=?, updated_at=NOW() WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setDouble(5, product.getCostPrice());
            ps.setString(6, product.getImagePath());
            ps.setBoolean(7, product.isAvailable());
            ps.setBoolean(8, product.isActive());
            ps.setInt(9, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.update failed", e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "UPDATE menu_items SET is_active = FALSE, updated_at = NOW() WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.delete failed", e);
        }
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM menu_items WHERE is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.count failed", e);
        }
    }

    @Override
    public long countActiveByCategory(int categoryId) {
        final String sql =
            "SELECT COUNT(*) FROM menu_items WHERE category_id = ? AND is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DatabaseException("ProductDao.countActiveByCategory failed", e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setImagePath(rs.getString("image_path"));
        p.setAvailable(rs.getBoolean("is_available"));
        p.setActive(rs.getBoolean("is_active"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) p.setUpdatedAt(ua.toLocalDateTime());
        return p;
    }
}

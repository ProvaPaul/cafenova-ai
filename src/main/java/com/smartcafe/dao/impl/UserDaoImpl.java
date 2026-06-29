package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.UserDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.PasswordResetToken;
import com.smartcafe.model.Role;
import com.smartcafe.model.User;

import java.sql.*;
import java.util.Optional;

/**
 * MySQL implementation of {@link UserDao}.
 *
 * Every query uses PreparedStatement with positional {@code ?} parameters.
 * Try-with-resources closes Connection, PreparedStatement, and ResultSet in
 * the right order even when exceptions are thrown.
 */
public class UserDaoImpl implements UserDao {

    // ── Read Operations ───────────────────────────────────────────────────────

    @Override
    public Optional<User> findById(int id) {
        final String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("findById failed", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        final String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("findByUsername failed", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        final String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("findByEmail failed", e);
        }
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        final String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("findByUsernameOrEmail failed", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        final String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("existsByUsername failed", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        final String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("existsByEmail failed", e);
        }
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DatabaseException("count failed", e);
        }
    }

    // ── Write Operations ──────────────────────────────────────────────────────

    @Override
    public User save(User user) {
        final String sql = """
                INSERT INTO users
                    (full_name, username, email, password_hash, phone, role, is_active, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, user.isActive());
            if (user.getCreatedBy() != null) {
                ps.setInt(8, user.getCreatedBy());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("save user failed", e);
        }
    }

    @Override
    public void updatePassword(int userId, String newPasswordHash) {
        final String sql = "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("updatePassword failed", e);
        }
    }

    // ── Password Reset Token Operations ──────────────────────────────────────

    @Override
    public void saveResetToken(PasswordResetToken token) {
        final String sql =
                "INSERT INTO password_reset_tokens (user_id, token, expires_at) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, token.getUserId());
            ps.setString(2, token.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(token.getExpiresAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) token.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("saveResetToken failed", e);
        }
    }

    @Override
    public Optional<PasswordResetToken> findValidResetToken(String token) {
        final String sql = """
                SELECT * FROM password_reset_tokens
                WHERE token = ? AND is_used = FALSE AND expires_at > NOW()
                LIMIT 1
                """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                PasswordResetToken prt = new PasswordResetToken();
                prt.setId(rs.getInt("id"));
                prt.setUserId(rs.getInt("user_id"));
                prt.setToken(rs.getString("token"));
                prt.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                prt.setUsed(rs.getBoolean("is_used"));
                Timestamp ca = rs.getTimestamp("created_at");
                if (ca != null) prt.setCreatedAt(ca.toLocalDateTime());
                return Optional.of(prt);
            }
        } catch (SQLException e) {
            throw new DatabaseException("findValidResetToken failed", e);
        }
    }

    @Override
    public void markTokenUsed(int tokenId) {
        final String sql = "UPDATE password_reset_tokens SET is_used = TRUE WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("markTokenUsed failed", e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setRole(Role.fromString(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) u.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) u.setUpdatedAt(uat.toLocalDateTime());
        int cb = rs.getInt("created_by");
        if (!rs.wasNull()) u.setCreatedBy(cb);
        return u;
    }
}

package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.NotificationDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.AppNotification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDaoImpl implements NotificationDao {

    @Override
    public void insert(AppNotification n) {
        final String sql =
            "INSERT INTO app_notifications (type, title, message, reference_id) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, n.getType());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage());
            if (n.getReferenceId() != null) ps.setInt(4, n.getReferenceId());
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("NotificationDao.insert", e); }
    }

    @Override
    public List<AppNotification> findUnread() {
        return query("SELECT * FROM app_notifications WHERE is_read = FALSE ORDER BY created_at DESC LIMIT 50", null);
    }

    @Override
    public List<AppNotification> findRecent(int limit) {
        return query("SELECT * FROM app_notifications ORDER BY created_at DESC LIMIT " + limit, null);
    }

    @Override
    public long countUnread() {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM app_notifications WHERE is_read = FALSE");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) { throw new DatabaseException("NotificationDao.countUnread", e); }
    }

    @Override
    public void markRead(int id) {
        exec("UPDATE app_notifications SET is_read = TRUE WHERE id = ?", id);
    }

    @Override
    public void markAllRead() {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE app_notifications SET is_read = TRUE")) {
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("NotificationDao.markAllRead", e); }
    }

    private void exec(String sql, int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("NotificationDao.exec", e); }
    }

    private List<AppNotification> query(String sql, Object param) {
        List<AppNotification> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AppNotification n = new AppNotification();
                    n.setId(rs.getInt("id"));
                    n.setType(rs.getString("type"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    int ref = rs.getInt("reference_id");
                    if (!rs.wasNull()) n.setReferenceId(ref);
                    n.setRead(rs.getBoolean("is_read"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
                    list.add(n);
                }
            }
        } catch (SQLException e) { throw new DatabaseException("NotificationDao.query", e); }
        return list;
    }
}

package com.smartcafe.service.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.DashboardStats;
import com.smartcafe.service.DashboardService;

import java.sql.*;

/**
 * Runs cross-table aggregate queries for the dashboard stats panel.
 * Each query is a separate PreparedStatement; COALESCE handles empty tables.
 */
public class DashboardServiceImpl implements DashboardService {

    @Override
    public DashboardStats getStats() {
        DashboardStats s = new DashboardStats();
        try (Connection c = DatabaseConfig.getConnection()) {
            s.setTodaySales(queryLong(c,
                "SELECT COUNT(*) FROM orders " +
                "WHERE DATE(created_at) = CURDATE() AND status IN ('SERVED','COMPLETED')"));

            s.setTodayRevenue(queryDouble(c,
                "SELECT COALESCE(SUM(amount_paid),0) FROM payments " +
                "WHERE DATE(paid_at) = CURDATE()"));

            s.setTodayOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()"));

            s.setTablesUsedToday(queryLong(c,
                "SELECT COUNT(DISTINCT table_id) FROM orders " +
                "WHERE DATE(created_at) = CURDATE() AND table_id IS NOT NULL"));

            s.setLowStockProducts(queryLong(c,
                "SELECT COUNT(*) FROM inventory_items " +
                "WHERE current_stock <= min_stock AND is_active = TRUE AND current_stock >= 0"));

            s.setTotalProducts(queryLong(c,
                "SELECT COUNT(*) FROM menu_items WHERE is_active = TRUE"));

        } catch (SQLException e) {
            throw new DatabaseException("DashboardService.getStats failed", e);
        }
        return s;
    }

    private static long queryLong(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private static double queryDouble(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }
}

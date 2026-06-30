package com.smartcafe.service.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.InventoryItemDao;
import com.smartcafe.dao.OrderDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.DashboardStats;
import com.smartcafe.model.InventoryItem;
import com.smartcafe.model.Order;
import com.smartcafe.service.DashboardService;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardServiceImpl implements DashboardService {

    private final InventoryItemDao inventoryItemDao;
    private final OrderDao         orderDao;

    public DashboardServiceImpl(InventoryItemDao inventoryItemDao, OrderDao orderDao) {
        this.inventoryItemDao = inventoryItemDao;
        this.orderDao         = orderDao;
    }

    // Backwards-compat no-arg constructor for existing callers that don't yet pass dependencies
    public DashboardServiceImpl() {
        this.inventoryItemDao = null;
        this.orderDao         = null;
    }

    @Override
    public DashboardStats getStats() {
        DashboardStats s = new DashboardStats();
        try (Connection c = DatabaseConfig.getConnection()) {
            // Totals
            s.setTodaySales(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE() AND status IN ('SERVED','COMPLETED')"));
            s.setTodayRevenue(queryDouble(c,
                "SELECT COALESCE(SUM(amount_paid),0) FROM payments WHERE DATE(paid_at) = CURDATE() AND payment_status='PAID'"));
            s.setTodayOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()"));
            s.setTablesUsedToday(queryLong(c,
                "SELECT COUNT(DISTINCT table_id) FROM orders WHERE DATE(created_at) = CURDATE() AND table_id IS NOT NULL"));
            s.setLowStockProducts(queryLong(c,
                "SELECT COUNT(*) FROM inventory_items WHERE current_stock <= min_stock AND is_active = TRUE"));
            s.setTotalProducts(queryLong(c,
                "SELECT COUNT(*) FROM menu_items WHERE is_active = TRUE"));

            // Status breakdown
            s.setPendingOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status IN ('NEW','PENDING') AND DATE(created_at) = CURDATE()"));
            s.setConfirmedOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status = 'CONFIRMED' AND DATE(created_at) = CURDATE()"));
            s.setPreparingOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status IN ('PREPARING','IN_PROGRESS') AND DATE(created_at) = CURDATE()"));
            s.setReadyOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status = 'READY' AND DATE(created_at) = CURDATE()"));
            s.setServedOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status = 'SERVED' AND DATE(created_at) = CURDATE()"));
            s.setCompletedOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED' AND DATE(created_at) = CURDATE()"));
            s.setCancelledOrders(queryLong(c,
                "SELECT COUNT(*) FROM orders WHERE status = 'CANCELLED' AND DATE(created_at) = CURDATE()"));

            // Recent orders (last 10)
            s.setRecentOrders(queryRecentOrders(c));

            // Top 5 selling products today
            s.setTopProducts(queryTopProducts(c));

            // Low-stock items list
            if (inventoryItemDao != null) {
                s.setLowStockItems(inventoryItemDao.findLowStock());
            } else {
                s.setLowStockItems(queryLowStockItems(c));
            }

        } catch (SQLException e) {
            throw new DatabaseException("DashboardService.getStats failed", e);
        }
        return s;
    }

    private List<Order> queryRecentOrders(Connection c) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.order_number, o.customer_name, o.order_type, o.status, " +
                     "o.total, o.created_at, ct.table_number " +
                     "FROM orders o LEFT JOIN cafe_tables ct ON o.table_id = ct.id " +
                     "ORDER BY o.created_at DESC LIMIT 10";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setOrderNumber(rs.getString("order_number"));
                o.setCustomerName(rs.getString("customer_name"));
                o.setOrderType(rs.getString("order_type"));
                o.setStatus(rs.getString("status"));
                o.setTotal(rs.getDouble("total"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
                o.setTableNumber(rs.getString("table_number"));
                list.add(o);
            }
        }
        return list;
    }

    private List<Map<String, Object>> queryTopProducts(Connection c) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT mi.name, SUM(oi.quantity) AS qty_sold, SUM(oi.subtotal) AS revenue " +
            "FROM order_items oi " +
            "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE DATE(o.created_at) = CURDATE() AND o.status IN ('COMPLETED','SERVED') " +
            "GROUP BY mi.id, mi.name ORDER BY qty_sold DESC LIMIT 5";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("qtySold", rs.getInt("qty_sold"));
                row.put("revenue", rs.getDouble("revenue"));
                list.add(row);
            }
        }
        return list;
    }

    private List<InventoryItem> queryLowStockItems(Connection c) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT ii.*, s.name AS supplier_name FROM inventory_items ii " +
                     "LEFT JOIN suppliers s ON ii.supplier_id = s.id " +
                     "WHERE ii.current_stock <= ii.min_stock AND ii.is_active = TRUE LIMIT 10";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setUnit(rs.getString("unit"));
                item.setCurrentStock(rs.getDouble("current_stock"));
                item.setMinStock(rs.getDouble("min_stock"));
                list.add(item);
            }
        }
        return list;
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

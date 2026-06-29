package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.OrderDao;
import com.smartcafe.dao.CafeTableDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDaoImpl implements OrderDao {

    private final CafeTableDao cafeTableDao;

    public OrderDaoImpl(CafeTableDao cafeTableDao) {
        this.cafeTableDao = cafeTableDao;
    }

    // ── Transactional order placement ─────────────────────────────────────────

    @Override
    public Order placeOrderTransactional(Order order, List<OrderItem> items, Payment payment)
            throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Insert order with temp order_number
            int orderId = insertOrder(conn, order);
            order.setId(orderId);

            // 2. Generate and set real order_number
            String orderNumber = "ORD-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", orderId);
            updateOrderNumber(conn, orderId, orderNumber);
            order.setOrderNumber(orderNumber);

            // 3. Insert items
            for (OrderItem item : items) {
                item.setOrderId(orderId);
                insertItem(conn, item);
            }

            // 4. Insert payment
            payment.setOrderId(orderId);
            insertPayment(conn, payment);

            // 5. Mark table OCCUPIED for dine-in
            if (order.getTableId() != null
                    && Order.TYPE_DINE_IN.equals(order.getOrderType())) {
                cafeTableDao.updateStatus(conn, order.getTableId(), CafeTable.STATUS_OCCUPIED);
            }

            conn.commit();
            order.setItems(items);
            order.setPayment(payment);
            return order;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { /* log */ }
            throw e;
        } finally {
            try { conn.close(); } catch (SQLException ex) { /* log */ }
        }
    }

    private int insertOrder(Connection conn, Order o) throws SQLException {
        final String sql =
            "INSERT INTO orders (order_number, table_id, cashier_id, customer_name, order_type," +
            " status, subtotal, tax, discount, total, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "PENDING");   // placeholder; updated in step 2
            if (o.getTableId() != null) ps.setInt(2, o.getTableId());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, o.getCashierId());
            ps.setString(4, o.getCustomerName());
            ps.setString(5, o.getOrderType());
            ps.setString(6, o.getStatus());
            ps.setDouble(7, o.getSubtotal());
            ps.setDouble(8, o.getTax());
            ps.setDouble(9, o.getDiscount());
            ps.setDouble(10, o.getTotal());
            ps.setString(11, o.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Order insert returned no generated key");
            }
        }
    }

    private void updateOrderNumber(Connection conn, int orderId, String orderNumber)
            throws SQLException {
        final String sql = "UPDATE orders SET order_number = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private void insertItem(Connection conn, OrderItem item) throws SQLException {
        final String sql =
            "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, notes, status)" +
            " VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getMenuItemId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getSubtotal());
            ps.setString(6, item.getNotes());
            ps.setString(7, item.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }
        }
    }

    private void insertPayment(Connection conn, Payment p) throws SQLException {
        final String sql =
            "INSERT INTO payments (order_id, payment_method, amount_paid, change_amount," +
            " transaction_ref, cashier_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setString(2, p.getPaymentMethod());
            ps.setDouble(3, p.getAmountPaid());
            ps.setDouble(4, p.getChangeAmount());
            ps.setString(5, p.getTransactionRef());
            ps.setInt(6, p.getCashierId());
            ps.executeUpdate();
        }
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    @Override
    public List<Order> findAll(int limit) {
        final String sql =
            "SELECT o.*, ct.table_number, u.full_name AS cashier_name " +
            "FROM orders o " +
            "LEFT JOIN cafe_tables ct ON o.table_id = ct.id " +
            "LEFT JOIN users u ON o.cashier_id = u.id " +
            "ORDER BY o.created_at DESC LIMIT ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) list.add(mapOrder(rs));
                return list;
            }
        } catch (SQLException e) { throw new DatabaseException("OrderDao.findAll", e); }
    }

    @Override
    public List<Order> findByDateRange(LocalDate from, LocalDate to, String status) {
        StringBuilder sb = new StringBuilder(
            "SELECT o.*, ct.table_number, u.full_name AS cashier_name " +
            "FROM orders o " +
            "LEFT JOIN cafe_tables ct ON o.table_id = ct.id " +
            "LEFT JOIN users u ON o.cashier_id = u.id " +
            "WHERE DATE(o.created_at) BETWEEN ? AND ? ");
        if (status != null && !status.isBlank() && !"ALL".equals(status))
            sb.append("AND o.status = ? ");
        sb.append("ORDER BY o.created_at DESC");

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            if (status != null && !status.isBlank() && !"ALL".equals(status))
                ps.setString(3, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) list.add(mapOrder(rs));
                return list;
            }
        } catch (SQLException e) { throw new DatabaseException("OrderDao.findByDateRange", e); }
    }

    @Override
    public Optional<Order> findById(int id) {
        final String sql =
            "SELECT o.*, ct.table_number, u.full_name AS cashier_name " +
            "FROM orders o " +
            "LEFT JOIN cafe_tables ct ON o.table_id = ct.id " +
            "LEFT JOIN users u ON o.cashier_id = u.id " +
            "WHERE o.id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapOrder(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("OrderDao.findById", e); }
    }

    @Override
    public List<OrderItem> findItemsByOrderId(int orderId) {
        final String sql =
            "SELECT oi.*, m.name AS product_name FROM order_items oi " +
            "JOIN menu_items m ON oi.menu_item_id = m.id " +
            "WHERE oi.order_id = ? ORDER BY oi.id ASC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> list = new ArrayList<>();
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setMenuItemId(rs.getInt("menu_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    item.setNotes(rs.getString("notes"));
                    item.setStatus(rs.getString("status"));
                    item.setProductName(rs.getString("product_name"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) item.setCreatedAt(ts.toLocalDateTime());
                    list.add(item);
                }
                return list;
            }
        } catch (SQLException e) { throw new DatabaseException("OrderDao.findItemsByOrderId", e); }
    }

    @Override
    public long countToday() {
        final String sql = "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) { throw new DatabaseException("OrderDao.countToday", e); }
    }

    @Override
    public void updateStatus(int orderId, String status) {
        final String sql = "UPDATE orders SET status=?, updated_at=NOW() WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, orderId); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("OrderDao.updateStatus", e); }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderNumber(rs.getString("order_number"));
        int tid = rs.getInt("table_id");
        if (!rs.wasNull()) o.setTableId(tid);
        o.setCashierId(rs.getInt("cashier_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setOrderType(rs.getString("order_type"));
        o.setStatus(rs.getString("status"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setTax(rs.getDouble("tax"));
        o.setDiscount(rs.getDouble("discount"));
        o.setTotal(rs.getDouble("total"));
        o.setNotes(rs.getString("notes"));
        o.setTableNumber(rs.getString("table_number"));
        o.setCashierName(rs.getString("cashier_name"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) o.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) o.setUpdatedAt(ua.toLocalDateTime());
        return o;
    }
}

package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.PaymentDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.Payment;

import java.sql.*;
import java.util.Optional;

public class PaymentDaoImpl implements PaymentDao {

    @Override
    public void save(Connection conn, Payment p) throws SQLException {
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

    @Override
    public Optional<Payment> findByOrderId(int orderId) {
        final String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Payment p = new Payment();
                p.setId(rs.getInt("id"));
                p.setOrderId(rs.getInt("order_id"));
                p.setPaymentMethod(rs.getString("payment_method"));
                p.setAmountPaid(rs.getDouble("amount_paid"));
                p.setChangeAmount(rs.getDouble("change_amount"));
                p.setTransactionRef(rs.getString("transaction_ref"));
                p.setCashierId(rs.getInt("cashier_id"));
                Timestamp ts = rs.getTimestamp("paid_at");
                if (ts != null) p.setPaidAt(ts.toLocalDateTime());
                return Optional.of(p);
            }
        } catch (SQLException e) { throw new DatabaseException("PaymentDao.findByOrderId", e); }
    }
}

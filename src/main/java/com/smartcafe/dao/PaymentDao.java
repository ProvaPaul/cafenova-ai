package com.smartcafe.dao;

import com.smartcafe.model.Payment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface PaymentDao {
    /** Insert payment inside an existing transaction connection. */
    void             save(Connection conn, Payment payment) throws SQLException;
    Optional<Payment> findByOrderId(int orderId);
}

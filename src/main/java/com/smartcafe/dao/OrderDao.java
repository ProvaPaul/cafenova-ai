package com.smartcafe.dao;

import com.smartcafe.model.Order;
import com.smartcafe.model.OrderItem;
import com.smartcafe.model.Payment;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderDao {

    /**
     * Saves order + items + payment atomically in one DB transaction.
     * Returns the fully populated Order (with generated id and order_number).
     */
    Order placeOrderTransactional(Order order, List<OrderItem> items, Payment payment)
            throws SQLException;

    // ── Read ──────────────────────────────────────────────────────────────────
    List<Order>     findAll(int limit);
    List<Order>     findByDateRange(LocalDate from, LocalDate to, String status);
    Optional<Order> findById(int id);

    /** Loads items for an order (with product name from menu_items JOIN). */
    List<OrderItem> findItemsByOrderId(int orderId);

    long countToday();   // used to generate sequential order numbers

    void updateStatus(int orderId, String status);
}

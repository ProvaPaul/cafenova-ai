package com.smartcafe.service;

import com.smartcafe.model.Order;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {
    /** Orders in a date range, optionally filtered by status ("ALL" or a specific status). */
    List<Order> findOrders(LocalDate from, LocalDate to, String status);

    /** Loads an order with its items and payment populated. */
    Order       findOrderDetails(int orderId);

    /** Marks an order as CANCELLED and frees the table if dine-in. */
    void        voidOrder(int orderId);
}

package com.smartcafe.service;

import com.smartcafe.model.CartItem;
import com.smartcafe.model.Order;
import com.smartcafe.model.OrderItem;

import java.util.List;

public interface OrderService {

    /**
     * Places an order atomically: saves order + items + payment in one DB transaction.
     *
     * @param cartItems      non-empty list of cart items
     * @param tableId        nullable; null for TAKEAWAY / DELIVERY
     * @param orderType      Order.TYPE_DINE_IN | TYPE_TAKEAWAY | TYPE_DELIVERY
     * @param customerName   optional walk-in name
     * @param notes          optional order notes
     * @param discountPct    0-100 percentage discount
     * @param paymentMethod  Payment.METHOD_CASH | METHOD_CARD | METHOD_MOBILE
     * @param amountPaid     cash tendered (must be >= total for CASH)
     * @param cashierId      logged-in user id
     * @return fully populated Order with order number, items, and payment
     */
    Order placeOrder(List<CartItem> cartItems, Integer tableId, String orderType,
                     String customerName, String notes, double discountPct,
                     String paymentMethod, double amountPaid, int cashierId);

    List<Order> findRecent(int limit);

    /** Returns all orders (no limit), newest first. */
    List<Order> findAll();

    /** Updates the order status directly (used by order management panel). */
    void updateStatus(int orderId, String newStatus);

    /** Returns the order items with product names populated. */
    List<OrderItem> findItemsByOrderId(int orderId);
}

package com.smartcafe.service.impl;

import com.smartcafe.dao.OrderDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.*;
import com.smartcafe.service.OrderService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService {

    private static final double TAX_RATE = 0.12; // 12% VAT

    private final OrderDao orderDao;

    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public Order placeOrder(List<CartItem> cartItems, Integer tableId, String orderType,
                            String customerName, String notes, double discountPct,
                            String paymentMethod, double amountPaid, int cashierId) {

        if (cartItems == null || cartItems.isEmpty())
            throw new ValidationException("Cart is empty — add at least one product");
        if (discountPct < 0 || discountPct > 100)
            throw new ValidationException("Discount must be between 0 and 100%");

        // Build OrderItems from cart
        List<OrderItem> items = cartItems.stream()
                .map(ci -> new OrderItem(
                        ci.getProduct().getId(),
                        ci.getProduct().getName(),
                        ci.getQuantity(),
                        ci.getProduct().getPrice()))
                .collect(Collectors.toList());

        // Compute financials
        double subtotal  = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        double discount  = Math.round(subtotal * (discountPct / 100.0) * 100.0) / 100.0;
        double taxable   = subtotal - discount;
        double tax       = Math.round(taxable * TAX_RATE * 100.0) / 100.0;
        double total     = Math.round((taxable + tax) * 100.0) / 100.0;

        if (Payment.METHOD_CASH.equals(paymentMethod) && amountPaid < total)
            throw new ValidationException(
                String.format("Amount paid (₱%.2f) is less than the total (₱%.2f)", amountPaid, total));

        double change = Payment.METHOD_CASH.equals(paymentMethod)
                ? Math.round((amountPaid - total) * 100.0) / 100.0 : 0.0;

        // Build Order
        Order order = new Order();
        order.setCashierId(cashierId);
        order.setTableId(tableId);
        order.setOrderType(orderType != null ? orderType : Order.TYPE_DINE_IN);
        order.setCustomerName(customerName != null && !customerName.isBlank()
                ? customerName.trim() : null);
        order.setNotes(notes != null && !notes.isBlank() ? notes.trim() : null);
        order.setStatus(Order.STATUS_COMPLETED); // POS = immediate checkout
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setTotal(total);

        // Build Payment
        Payment payment = new Payment(0, paymentMethod, amountPaid, change, cashierId);

        try {
            return orderDao.placeOrderTransactional(order, items, payment);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to place order", e);
        }
    }

    @Override
    public List<Order> findRecent(int limit) {
        return orderDao.findAll(limit);
    }

    @Override
    public List<Order> findAll() {
        return orderDao.findAll(1000);
    }

    @Override
    public void updateStatus(int orderId, String newStatus) {
        orderDao.updateStatus(orderId, newStatus);
    }

    @Override
    public List<OrderItem> findItemsByOrderId(int orderId) {
        return orderDao.findItemsByOrderId(orderId);
    }
}

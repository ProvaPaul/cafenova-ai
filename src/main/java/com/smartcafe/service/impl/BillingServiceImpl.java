package com.smartcafe.service.impl;

import com.smartcafe.dao.CafeTableDao;
import com.smartcafe.dao.OrderDao;
import com.smartcafe.dao.PaymentDao;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.CafeTable;
import com.smartcafe.model.Order;
import com.smartcafe.service.BillingService;

import java.time.LocalDate;
import java.util.List;

public class BillingServiceImpl implements BillingService {

    private final OrderDao     orderDao;
    private final PaymentDao   paymentDao;
    private final CafeTableDao cafeTableDao;

    public BillingServiceImpl(OrderDao orderDao, PaymentDao paymentDao,
                              CafeTableDao cafeTableDao) {
        this.orderDao     = orderDao;
        this.paymentDao   = paymentDao;
        this.cafeTableDao = cafeTableDao;
    }

    @Override
    public List<Order> findOrders(LocalDate from, LocalDate to, String status) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to   == null) to   = LocalDate.now();
        return orderDao.findByDateRange(from, to, status);
    }

    @Override
    public Order findOrderDetails(int orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order #" + orderId + " not found"));
        order.setItems(orderDao.findItemsByOrderId(orderId));
        paymentDao.findByOrderId(orderId).ifPresent(order::setPayment);
        return order;
    }

    @Override
    public void voidOrder(int orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found"));
        if (Order.STATUS_CANCELLED.equals(order.getStatus()))
            throw new ValidationException("Order is already cancelled");

        orderDao.updateStatus(orderId, Order.STATUS_CANCELLED);

        // Free table if it was dine-in
        if (order.getTableId() != null && Order.TYPE_DINE_IN.equals(order.getOrderType())) {
            cafeTableDao.updateStatus(order.getTableId(), CafeTable.STATUS_AVAILABLE);
        }
    }
}

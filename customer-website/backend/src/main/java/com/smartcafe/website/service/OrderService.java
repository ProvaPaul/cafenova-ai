package com.smartcafe.website.service;

import com.smartcafe.website.dto.request.CheckoutRequest;
import com.smartcafe.website.dto.response.OrderResponse;
import com.smartcafe.website.entity.*;
import com.smartcafe.website.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;
    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;

    private static final AtomicInteger SEQ = new AtomicInteger((int)(System.currentTimeMillis() % 10000));

    @Transactional
    public OrderResponse checkout(Long customerId, CheckoutRequest req) {
        List<CartItemEntity> cart = cartRepository.findByCustomerId(customerId);
        if (cart.isEmpty()) throw new IllegalArgumentException("Cart is empty");

        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Build order items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemEntity> items = new ArrayList<>();
        for (CartItemEntity ci : cart) {
            MenuItemEntity mi = ci.getMenuItem();
            BigDecimal lineTotal = mi.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            items.add(OrderItemEntity.builder()
                    .menuItemId(mi.getId())
                    .menuItemName(mi.getName())
                    .quantity(ci.getQuantity())
                    .unitPrice(mi.getPrice())
                    .subtotal(lineTotal)
                    .status("PENDING")
                    .build());
        }

        // Apply coupon
        BigDecimal discount = BigDecimal.ZERO;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            var couponOpt = couponRepository.findByCodeAndActiveTrue(req.getCouponCode());
            if (couponOpt.isPresent()) {
                CouponEntity c = couponOpt.get();
                if (subtotal.compareTo(c.getMinOrder()) >= 0) {
                    if (c.getDiscountPct() != null)
                        discount = subtotal.multiply(c.getDiscountPct()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    else if (c.getDiscountAmt() != null)
                        discount = c.getDiscountAmt();
                    c.setUsedCount(c.getUsedCount() + 1);
                    couponRepository.save(c);
                }
            }
        }

        BigDecimal tax = subtotal.subtract(discount)
                .multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discount).add(tax);

        String orderNum = "WEB-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                          + "-" + String.format("%04d", SEQ.incrementAndGet() % 10000);

        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNum)
                .customerId(customerId)
                .customerName(customer.getFullName())
                .orderType(req.getOrderType())
                .status("PENDING")
                .subtotal(subtotal)
                .tax(tax)
                .discount(discount)
                .total(total)
                .notes(req.getNotes())
                .build();
        order = orderRepository.save(order);

        final OrderEntity savedOrder = order;
        items.forEach(i -> i.setOrder(savedOrder));
        savedOrder.setItems(items);
        orderRepository.save(savedOrder);

        // Clear cart
        cartRepository.deleteByCustomerId(customerId);

        // Add loyalty points (1 per ₱10)
        int pts = total.divide(BigDecimal.TEN, 0, RoundingMode.FLOOR).intValue();
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pts);
        customer.setVisitCount(customer.getVisitCount() + 1);
        customer.setTotalSpent(customer.getTotalSpent().add(total));
        customerRepository.save(customer);

        // Send notification
        notificationRepository.save(NotificationEntity.builder()
                .customerId(customerId)
                .title("Order Placed")
                .message("Your order " + orderNum + " has been received. Total: ₱" + total)
                .type("ORDER")
                .build());

        return toResponse(savedOrder);
    }

    public List<OrderResponse> getHistory(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrder(Long customerId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getCustomerId().equals(customerId))
            throw new IllegalArgumentException("Unauthorized");
        return toResponse(order);
    }

    private OrderResponse toResponse(OrderEntity o) {
        List<OrderResponse.OrderItemResponse> itemResponses = o.getItems() == null ? List.of() :
                o.getItems().stream().map(i -> {
                    String name = i.getMenuItemName();
                    if (name == null) {
                        name = menuItemRepository.findById(i.getMenuItemId())
                                .map(MenuItemEntity::getName).orElse("Unknown");
                    }
                    return OrderResponse.OrderItemResponse.builder()
                            .id(i.getId()).menuItemName(name)
                            .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                            .subtotal(i.getSubtotal()).build();
                }).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber())
                .orderType(o.getOrderType()).status(o.getStatus())
                .subtotal(o.getSubtotal()).tax(o.getTax())
                .discount(o.getDiscount()).total(o.getTotal())
                .notes(o.getNotes()).createdAt(o.getCreatedAt())
                .items(itemResponses).build();
    }
}

package com.smartcafe.website.service;

import com.smartcafe.website.entity.*;
import com.smartcafe.website.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    public Map<String, Object> getLoyaltyInfo(Long customerId) {
        CustomerEntity c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        String tier = tier(c.getLoyaltyPoints());
        int nextTierPts = nextTierPoints(c.getLoyaltyPoints());
        return Map.of(
                "points", c.getLoyaltyPoints(),
                "totalSpent", c.getTotalSpent(),
                "visitCount", c.getVisitCount(),
                "tier", tier,
                "nextTierPoints", nextTierPts,
                "pointsToNextTier", Math.max(0, nextTierPts - c.getLoyaltyPoints())
        );
    }

    public List<CouponEntity> getAvailableCoupons() {
        return couponRepository.findAll().stream()
                .filter(CouponEntity::isActive)
                .filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(java.time.LocalDateTime.now()))
                .toList();
    }

    @Transactional
    public CouponEntity redeemPoints(Long customerId, int points) {
        CustomerEntity c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (c.getLoyaltyPoints() < points)
            throw new IllegalArgumentException("Insufficient points");
        if (points < 100)
            throw new IllegalArgumentException("Minimum redemption is 100 points");

        c.setLoyaltyPoints(c.getLoyaltyPoints() - points);
        customerRepository.save(c);

        // Generate coupon: 100 pts = ₱50 discount
        int discountAmt = (points / 100) * 50;
        String code = "LOYALTY-" + customerId + "-" + System.currentTimeMillis() % 10000;
        CouponEntity coupon = CouponEntity.builder()
                .code(code)
                .description("Redeemed " + points + " loyalty points")
                .discountAmt(java.math.BigDecimal.valueOf(discountAmt))
                .minOrder(java.math.BigDecimal.valueOf(100))
                .maxUses(1)
                .active(true)
                .expiresAt(java.time.LocalDateTime.now().plusDays(30))
                .build();
        coupon = couponRepository.save(coupon);

        notificationRepository.save(NotificationEntity.builder()
                .customerId(customerId)
                .title("Points Redeemed")
                .message("You redeemed " + points + " points for a ₱" + discountAmt + " coupon: " + code)
                .type("LOYALTY")
                .build());

        return coupon;
    }

    public List<Map<String, Object>> getPointsHistory(Long customerId) {
        List<Map<String, Object>> history = new ArrayList<>();
        orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).forEach(o -> {
            int pts = o.getTotal().divide(BigDecimal.TEN, 0, RoundingMode.FLOOR).intValue();
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", o.getId());
            entry.put("points", pts);
            entry.put("description", "Order " + o.getOrderNumber());
            entry.put("createdAt", o.getCreatedAt());
            history.add(entry);
        });
        return history;
    }

    private String tier(int pts) {
        if (pts >= 5000) return "Platinum";
        if (pts >= 2000) return "Gold";
        if (pts >= 500)  return "Silver";
        return "Bronze";
    }

    private int nextTierPoints(int pts) {
        if (pts >= 5000) return 5000;
        if (pts >= 2000) return 5000;
        if (pts >= 500)  return 2000;
        return 500;
    }
}

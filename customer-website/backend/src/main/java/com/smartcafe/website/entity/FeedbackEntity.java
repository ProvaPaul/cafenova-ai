package com.smartcafe.website.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "menu_item_id")
    private Long menuItemId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "rating", nullable = false)
    private int rating = 5;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String customerName;

    @Transient
    private String menuItemName;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}

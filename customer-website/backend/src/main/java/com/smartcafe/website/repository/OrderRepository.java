package com.smartcafe.website.repository;

import com.smartcafe.website.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}

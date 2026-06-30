package com.smartcafe.website.repository;

import com.smartcafe.website.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByCustomerIdAndReadFalse(Long customerId);
}

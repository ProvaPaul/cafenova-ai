package com.smartcafe.website.repository;

import com.smartcafe.website.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByCustomerId(Long customerId);
    Optional<CartItemEntity> findByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);
    void deleteByCustomerId(Long customerId);
}

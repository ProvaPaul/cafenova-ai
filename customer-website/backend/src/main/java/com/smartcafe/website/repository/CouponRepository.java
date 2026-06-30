package com.smartcafe.website.repository;

import com.smartcafe.website.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
    Optional<CouponEntity> findByCodeAndActiveTrue(String code);
}

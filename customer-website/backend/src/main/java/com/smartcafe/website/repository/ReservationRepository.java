package com.smartcafe.website.repository;

import com.smartcafe.website.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByCustomerIdOrderByReservationDateDescReservationTimeDesc(Long customerId);
}

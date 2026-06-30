package com.smartcafe.website.repository;

import com.smartcafe.website.entity.CafeTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeTableRepository extends JpaRepository<CafeTableEntity, Long> {
    List<CafeTableEntity> findByStatusOrderByTableNumberAsc(String status);
    List<CafeTableEntity> findAllByOrderByTableNumberAsc();
}

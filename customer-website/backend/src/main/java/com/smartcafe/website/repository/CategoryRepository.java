package com.smartcafe.website.repository;

import com.smartcafe.website.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByActiveTrueOrderBySortOrderAsc();
}

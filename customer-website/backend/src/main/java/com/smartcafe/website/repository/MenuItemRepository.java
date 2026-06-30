package com.smartcafe.website.repository;

import com.smartcafe.website.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, Long> {

    List<MenuItemEntity> findByActiveTrueAndAvailableTrueOrderByNameAsc();

    List<MenuItemEntity> findByCategoryIdAndActiveTrueAndAvailableTrue(Long categoryId);

    @Query("SELECT m FROM MenuItemEntity m WHERE m.active = true AND m.available = true " +
           "AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<MenuItemEntity> search(@Param("q") String query);
}

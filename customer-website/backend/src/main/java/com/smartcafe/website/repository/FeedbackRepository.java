package com.smartcafe.website.repository;

import com.smartcafe.website.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    List<FeedbackEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<FeedbackEntity> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);

    @Query("SELECT AVG(f.rating) FROM FeedbackEntity f WHERE f.menuItemId = :itemId")
    Double avgRatingByMenuItemId(@Param("itemId") Long menuItemId);
}

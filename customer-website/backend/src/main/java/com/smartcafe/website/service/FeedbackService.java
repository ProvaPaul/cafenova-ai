package com.smartcafe.website.service;

import com.smartcafe.website.dto.request.FeedbackRequest;
import com.smartcafe.website.entity.FeedbackEntity;
import com.smartcafe.website.repository.CustomerRepository;
import com.smartcafe.website.repository.FeedbackRepository;
import com.smartcafe.website.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public FeedbackEntity submit(Long customerId, FeedbackRequest req) {
        FeedbackEntity fb = FeedbackEntity.builder()
                .customerId(customerId)
                .menuItemId(req.getMenuItemId())
                .orderId(req.getOrderId())
                .rating(req.getRating())
                .review(req.getReview())
                .build();
        return feedbackRepository.save(fb);
    }

    public List<FeedbackEntity> getMyFeedback(Long customerId) {
        List<FeedbackEntity> list = feedbackRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        list.forEach(f -> {
            if (f.getMenuItemId() != null)
                menuItemRepository.findById(f.getMenuItemId())
                        .ifPresent(m -> f.setMenuItemName(m.getName()));
        });
        return list;
    }

    public List<FeedbackEntity> getItemFeedback(Long menuItemId) {
        List<FeedbackEntity> list = feedbackRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId);
        list.forEach(f -> {
            if (f.getCustomerId() != null)
                customerRepository.findById(f.getCustomerId())
                        .ifPresent(c -> f.setCustomerName(c.getFullName()));
        });
        return list;
    }
}

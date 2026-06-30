package com.smartcafe.website.service;

import com.smartcafe.website.entity.NotificationEntity;
import com.smartcafe.website.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationEntity> getAll(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Map<String, Long> getUnreadCount(Long customerId) {
        return Map.of("count", notificationRepository.countByCustomerIdAndReadFalse(customerId));
    }

    @Transactional
    public void markAllRead(Long customerId) {
        notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .forEach(n -> { n.setRead(true); notificationRepository.save(n); });
    }

    @Transactional
    public void markRead(Long customerId, Long notificationId) {
        NotificationEntity n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getCustomerId().equals(customerId))
            throw new IllegalArgumentException("Unauthorized");
        n.setRead(true);
        notificationRepository.save(n);
    }
}

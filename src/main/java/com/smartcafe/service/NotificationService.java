package com.smartcafe.service;

import com.smartcafe.model.AppNotification;
import java.util.List;

public interface NotificationService {
    void push(String type, String title, String message, Integer referenceId);
    List<AppNotification> getUnread();
    List<AppNotification> getRecent(int limit);
    long countUnread();
    void markRead(int id);
    void markAllRead();
}

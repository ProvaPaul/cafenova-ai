package com.smartcafe.dao;

import com.smartcafe.model.AppNotification;
import java.util.List;

public interface NotificationDao {
    void insert(AppNotification n);
    List<AppNotification> findUnread();
    List<AppNotification> findRecent(int limit);
    long countUnread();
    void markRead(int id);
    void markAllRead();
}

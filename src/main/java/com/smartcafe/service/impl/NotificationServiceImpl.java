package com.smartcafe.service.impl;

import com.smartcafe.dao.NotificationDao;
import com.smartcafe.model.AppNotification;
import com.smartcafe.service.NotificationService;

import java.util.List;

public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao dao;

    public NotificationServiceImpl(NotificationDao dao) { this.dao = dao; }

    @Override
    public void push(String type, String title, String message, Integer referenceId) {
        dao.insert(new AppNotification(type, title, message, referenceId));
    }

    @Override public List<AppNotification> getUnread()         { return dao.findUnread(); }
    @Override public List<AppNotification> getRecent(int n)    { return dao.findRecent(n); }
    @Override public long countUnread()                         { return dao.countUnread(); }
    @Override public void markRead(int id)                      { dao.markRead(id); }
    @Override public void markAllRead()                         { dao.markAllRead(); }
}

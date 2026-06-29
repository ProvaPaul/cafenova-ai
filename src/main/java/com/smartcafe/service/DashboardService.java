package com.smartcafe.service;

import com.smartcafe.model.DashboardStats;

public interface DashboardService {

    /**
     * Runs several aggregate queries and returns a single stats snapshot.
     * Designed to be called from a SwingWorker (off the EDT).
     */
    DashboardStats getStats();
}

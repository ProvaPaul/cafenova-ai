package com.smartcafe.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/** Aggregated stats returned by {@code DashboardService.getStats()}. */
public class DashboardStats {

    // Totals
    private long   todaySales;
    private double todayRevenue;
    private long   todayOrders;
    private long   tablesUsedToday;
    private long   lowStockProducts;
    private long   totalProducts;

    // Order status breakdown
    private long pendingOrders;
    private long confirmedOrders;
    private long preparingOrders;
    private long readyOrders;
    private long servedOrders;
    private long completedOrders;
    private long cancelledOrders;

    // Lists for dashboard widgets
    private List<Order>  recentOrders   = new ArrayList<>();
    private List<Map<String, Object>> topProducts = new ArrayList<>();
    private List<InventoryItem> lowStockItems = new ArrayList<>();

    // Getters / setters
    public long   getTodaySales()                       { return todaySales; }
    public void   setTodaySales(long v)                 { todaySales = v; }
    public double getTodayRevenue()                     { return todayRevenue; }
    public void   setTodayRevenue(double v)             { todayRevenue = v; }
    public long   getTodayOrders()                      { return todayOrders; }
    public void   setTodayOrders(long v)                { todayOrders = v; }
    public long   getTablesUsedToday()                  { return tablesUsedToday; }
    public void   setTablesUsedToday(long v)            { tablesUsedToday = v; }
    public long   getLowStockProducts()                 { return lowStockProducts; }
    public void   setLowStockProducts(long v)           { lowStockProducts = v; }
    public long   getTotalProducts()                    { return totalProducts; }
    public void   setTotalProducts(long v)              { totalProducts = v; }
    public long   getPendingOrders()                    { return pendingOrders; }
    public void   setPendingOrders(long v)              { pendingOrders = v; }
    public long   getConfirmedOrders()                  { return confirmedOrders; }
    public void   setConfirmedOrders(long v)            { confirmedOrders = v; }
    public long   getPreparingOrders()                  { return preparingOrders; }
    public void   setPreparingOrders(long v)            { preparingOrders = v; }
    public long   getReadyOrders()                      { return readyOrders; }
    public void   setReadyOrders(long v)                { readyOrders = v; }
    public long   getServedOrders()                     { return servedOrders; }
    public void   setServedOrders(long v)               { servedOrders = v; }
    public long   getCompletedOrders()                  { return completedOrders; }
    public void   setCompletedOrders(long v)            { completedOrders = v; }
    public long   getCancelledOrders()                  { return cancelledOrders; }
    public void   setCancelledOrders(long v)            { cancelledOrders = v; }
    public List<Order>  getRecentOrders()               { return recentOrders; }
    public void   setRecentOrders(List<Order> v)        { recentOrders = v; }
    public List<Map<String, Object>> getTopProducts()   { return topProducts; }
    public void   setTopProducts(List<Map<String, Object>> v) { topProducts = v; }
    public List<InventoryItem> getLowStockItems()       { return lowStockItems; }
    public void   setLowStockItems(List<InventoryItem> v){ lowStockItems = v; }

    public long getActiveOrders() { return pendingOrders + confirmedOrders + preparingOrders + readyOrders; }
}

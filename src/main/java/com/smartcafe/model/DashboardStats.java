package com.smartcafe.model;

/** Aggregated stats returned by {@code DashboardService.getStats()}. */
public class DashboardStats {

    private long   todaySales;       // completed / served orders today
    private double todayRevenue;     // ₱ sum from payments today
    private long   todayOrders;      // all orders placed today
    private long   tablesUsedToday;  // distinct tables with orders today
    private long   lowStockProducts; // inventory items at or below min_stock
    private long   totalProducts;    // active menu items

    public long   getTodaySales()                    { return todaySales; }
    public void   setTodaySales(long v)              { this.todaySales = v; }
    public double getTodayRevenue()                  { return todayRevenue; }
    public void   setTodayRevenue(double v)          { this.todayRevenue = v; }
    public long   getTodayOrders()                   { return todayOrders; }
    public void   setTodayOrders(long v)             { this.todayOrders = v; }
    public long   getTablesUsedToday()               { return tablesUsedToday; }
    public void   setTablesUsedToday(long v)         { this.tablesUsedToday = v; }
    public long   getLowStockProducts()              { return lowStockProducts; }
    public void   setLowStockProducts(long v)        { this.lowStockProducts = v; }
    public long   getTotalProducts()                 { return totalProducts; }
    public void   setTotalProducts(long v)           { this.totalProducts = v; }
}

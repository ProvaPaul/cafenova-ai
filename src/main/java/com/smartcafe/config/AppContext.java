package com.smartcafe.config;

import com.smartcafe.service.*;

import java.util.Objects;

/**
 * Lightweight static service-locator for a single-process desktop app.
 *
 * Why not pass services through every constructor?
 * At this scale (one JVM, one user) the constructor chain would propagate all
 * services through MainFrame → AdminDashboard → CategoryPanel → CategoryFormDialog,
 * creating deep coupling.  A single static registry avoids the chain while still
 * being deterministic (initialized once in Main before the EDT starts).
 *
 * Thread-safety: volatile fields + the single invocation in Main.java guarantee
 * correct publication across threads (EDT + SwingWorker).
 */
public final class AppContext {

    private static volatile CategoryService  categoryService;
    private static volatile ProductService   productService;
    private static volatile DashboardService dashboardService;
    private static volatile SupplierService  supplierService;
    private static volatile InventoryService inventoryService;
    private static volatile OrderService     orderService;
    private static volatile BillingService   billingService;

    private AppContext() {}

    public static void initialize(CategoryService cs, ProductService ps, DashboardService ds) {
        categoryService  = cs;
        productService   = ps;
        dashboardService = ds;
    }

    public static void initializeStep3(SupplierService ss, InventoryService is,
                                       OrderService os, BillingService bs) {
        supplierService  = ss;
        inventoryService = is;
        orderService     = os;
        billingService   = bs;
    }

    public static CategoryService  categoryService()  { return require(categoryService,  "categoryService"); }
    public static ProductService   productService()   { return require(productService,   "productService"); }
    public static DashboardService dashboardService() { return require(dashboardService, "dashboardService"); }
    public static SupplierService  supplierService()  { return require(supplierService,  "supplierService"); }
    public static InventoryService inventoryService() { return require(inventoryService, "inventoryService"); }
    public static OrderService     orderService()     { return require(orderService,     "orderService"); }
    public static BillingService   billingService()   { return require(billingService,   "billingService"); }

    private static <T> T require(T svc, String name) {
        return Objects.requireNonNull(svc, "AppContext: " + name + " not initialized — call initialize/initializeStep3 first");
    }
}
